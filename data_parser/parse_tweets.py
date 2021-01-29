import glob
import os
import shutil
import re
import ujson
import time
from datetime import datetime
from dateutil import tz

from ftfy import fix_text
from tqdm import tqdm

from utils import parse_int
from utils import clean_line
from utils import add_failure
from utils import get_tweet_data

import GetOldTweets3 as got

from settings import MAX_OUT_FILE_SIZE
from settings import TRAINING_NAME
from settings import OUT_DIR
from settings import MIN_LINE_LEN
from settings import MAX_LINE_LEN
from settings import API_DATA_MIN
from settings import API_DATA_MAX
from settings import API_GRANULARITY_TRIES_MAX

from settings import START_LINE_TOKEN
from settings import END_LINE_TOKEN
from settings import COMBINE_LINES

from granularity import Granularity
from granularity import increase_granularity
from granularity import decrease_granularity

START_TIME = 1143331200 # Twitter creation date

USERS = None
BLACKLISTED_WORDS = None

usr = [
    "EldritchSwift"
]
blk_w = [
    "@",
    "#"
]

USERS = set(user.strip().lower() for user in usr)
BLACKLISTED_WORDS = set(word.strip().lower() for word in blk_w)

tweets = set([])

def main():
    global OUT_DIR, USERS, tweets

    print("Parsing tweet data..")
    if os.path.exists(OUT_DIR):
        shutil.rmtree(OUT_DIR)
    os.makedirs(OUT_DIR + os.sep + "raw")
    for user in USERS:
        get_tweets(user, OUT_DIR + os.sep + "raw")
    
    print("Dumping finalized data..")
    files = glob.glob(OUT_DIR + os.sep + "raw" + os.sep + "*.dat")
    files.sort(key=os.path.getmtime)
    for file in files:
        parse_file(tweets, file)
    dump_data(tweets, OUT_DIR + os.sep + "out.txt")

    print("Done!")

def get_tweets(user, out_dir):
    global BLACKLISTED_WORDS, START_TIME, API_DATA_MIN, API_DATA_MAX, API_GRANULARITY_TRIES_MAX, MIN_LINE_LEN, MAX_LINE_LEN, START_LINE_TOKEN, END_LINE_TOKEN, COMBINE_LINES

    print("Fetching tweets for @" + user)

    begin = START_TIME
    granularity = int(Granularity.YEARLY.value)
    end = begin + granularity
    granularity_tries = 0

    use_blacklisted_words = len(BLACKLISTED_WORDS) > 0
    tweet_buffer = []
    num_tweets = 0

    current = 1
    out_file = None
    files = glob.glob(out_dir + os.sep + "*.dat")
    if len(files) == 0:
        out_file = out_dir + os.path.sep + str(current) + ".dat"
    else:
        out_file = max(files, key=os.path.getctime)
        current = int(os.path.splitext(os.path.basename(out_file))[0])
    
    while os.path.exists(out_file) and os.stat(out_file).st_size > 100 * 1024 * 1024:
        current += 1
        out_file = out_dir + os.path.sep + str(current) + ".dat"

    print("Granularity set to " + Granularity(granularity).name)

    final_time = int(time.time())
    while begin < final_time:
        print("Getting data from " + datetime.fromtimestamp(begin, tz=tz.gettz("UTC")).strftime("%B %d, %Y (%I:%M%p)") + " to " + datetime.fromtimestamp(end, tz=tz.gettz("UTC")).strftime("%B %d, %Y (%I:%M%p)") + "..")
        data = get_tweet_data(user, begin, end)

        if data is None:
            print("Failed to get response, skipping..")
            begin = end
            end = begin + granularity
            continue

        if len(data) <= API_DATA_MIN and granularity < int(Granularity.YEARLY.value):
            # Not enough results, we may want to think about decreasing granularity
            if granularity_tries > API_GRANULARITY_TRIES_MAX:
                granularity = decrease_granularity(granularity)
                print("Granularity set to " + Granularity(granularity).name)
                granularity_tries = 0
                # Don't set begin- we need to go back in time to collect things we would otherwise miss
                end = begin + granularity
                continue
            else:
                granularity_tries += 1
        elif len(data) >= API_DATA_MAX and granularity > int(Granularity.QUARTER_MIN.value):
            # Too many results, we need to increase granularity
            granularity_tries = 0
            granularity = increase_granularity(granularity)
            print("Granularity set to " + Granularity(granularity).name)
            # Don't set begin- we need to go back in time to collect things we missed
            end = begin + granularity
            continue
        else:
            granularity_tries = 0

        for tweet in data:
            body = clean_line(tweet.text, COMBINE_LINES)

            if body is None:
                continue
            if body == "<url>":
                continue
            body_len = len(body)
            if body_len < MIN_LINE_LEN or body_len > MAX_LINE_LEN:
                continue

            body_lower = body.lower()
            if use_blacklisted_words:
                cont_proper = False
                for s in BLACKLISTED_WORDS:
                    if s in body_lower:
                        cont_proper = True
                        break
                if cont_proper:
                    continue
            
            tweet_buffer.append(body) # Faster than insert()

            num_tweets += 1
            if num_tweets % 100 == 0:
                with open(out_file, "at", encoding="utf8") as outp:
                    for b in tweet_buffer:
                        outp.write(START_LINE_TOKEN + b + END_LINE_TOKEN + '\n')
                while os.path.exists(out_file) and os.stat(out_file).st_size > 100 * 1024 * 1024:
                    current += 1
                    out_file = out_dir + os.path.sep + str(current) + ".dat"
                tweet_buffer *= 0 # Very fast clear()

                print("Wrote " + str(num_tweets) + " tweets")

        begin = end
        end = begin + granularity
    
    if len(tweet_buffer) > 0:
        with open(out_file, "at", encoding="utf8") as outp:
            for b in tweet_buffer:
                outp.write(START_LINE_TOKEN + b + END_LINE_TOKEN + '\n')
        print("Wrote " + str(num_tweets) + " tweets")

def parse_file(data, in_file):
    global COMBINE_LINES, END_LINE_TOKEN

    print("Gathering tweets from " + in_file)
    size = 0

    if os.path.exists(in_file + ".wc"):
        with open(in_file + ".wc", "rt", encoding="utf8") as size_inp:
            size = parse_int(size_inp.readline())
    if size == 0:
        with open(in_file + ".wc", "wt", encoding="utf8") as size_outp:
            with open(in_file, 'rt', encoding="utf8") as inp:
                size = sum(1 for line in inp)
            size_outp.write(str(size) + "\n")
    with open(in_file, "rt", encoding="utf8") as inp:
        curr_text = ""
        end_l = END_LINE_TOKEN + '\n'
        for text in tqdm(inp, total=size, unit_scale=True):
            if COMBINE_LINES:
                data.add(text)
            else:
                curr_text += text
                if text.endswith(end_l):
                    data.add(curr_text)
                    curr_text = ""

def dump_data(data, out_file):
    global MAX_OUT_FILE_SIZE

    data_num = 0
    
    with open(out_file, "wt", encoding="utf8") as outp:
        for d in tqdm(data, total=len(data), unit_scale=True):
            outp.write(d)
            data_num += 1
            if data_num % 10000 == 0:
                outp.flush()
                if os.stat(out_file).st_size > MAX_OUT_FILE_SIZE:
                    return

if __name__ == "__main__":
    main()