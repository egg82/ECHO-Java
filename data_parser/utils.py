import time
import ujson
import os
import requests
import re
import GetOldTweets3 as got
from datetime import datetime
from dateutil import tz

from settings import GET_HEADERS
from settings import FAILURES_FILE
from settings import TIME_FILE
from settings import MIN_LINE_LEN
from settings import MAX_LINE_LEN

from tqdm import tqdm
from ftfy import fix_text

def is_number(inp):
    if inp.isnumeric():
        return True
    try:
        float(inp)
        return True
    except ValueError:
        return False

def parse_int(inp):
    if inp is None:
        return 0
    inp = inp.strip().replace(',', "")
    if len(inp) == 0:
        return 0
    if not is_number(inp):
        return 0
    return int(inp)

def add_failure(data):
    global FAILURES_FILE

    if not os.path.exists(FAILURES_FILE):
        with open(FAILURES_FILE, "wt", encoding="utf8") as file:
            file.write(data + "\n")
    else:
        with open(FAILURES_FILE, "at", encoding="utf8") as file:
            file.write(data + "\n")

def get_data(subreddit, data_type, begin, end):
    global GET_HEADERS

    tries = 0
    while True:
        req = requests.get("https://api.pushshift.io/reddit/" + data_type + "/search/?after=" + str(int(begin)) + "&before=" + str(int(end)) + "&subreddit=" + subreddit, headers=GET_HEADERS)
        if req.status_code == 429:
            time.sleep(0.5)
        else:
            ret_val = get_text(req)
            if not ret_val is None:
                return ret_val
            elif ret_val is None and tries >= 3:
                return ret_val
            else:
                tries += 1
                time.sleep(3.0)

def get_tweet_data(user, begin, end):
    tweetCriteria = got.manager.TweetCriteria().setUsername(user).setMaxTweets(25).setSince(datetime.fromtimestamp(begin, tz=tz.gettz("UTC")).strftime("%Y-%m-%d")).setUntil(datetime.fromtimestamp(end, tz=tz.gettz("UTC")).strftime("%Y-%m-%d")).setEmoji("unicode")
    return got.manager.TweetManager.getTweets(tweetCriteria)

RE_R = re.compile(r'\r\n')
RE_TABS = re.compile(r'[ \t]+')
RE_TABS_LINES = re.compile(r'[ \t\r\n]+')
RE_LINES = re.compile(r'[\r\n]{3,}')
RE_TRIM = re.compile(r'^[ \t]*(.*)[ \t]*$', re.MULTILINE)
RE_LINKS = re.compile(r'\[+(.+)\]+\s*\(+https?:\/\/.*\)+')
RE_LINKS_2 = re.compile(r'https?://.*')
RE_LINKS_3 = re.compile(r'!\[+img\]+\s*\(+[a-z0-9]*\)+')

def clean_line(line, combine_lines, use_len = True):
    global RE_TABS, RE_LINKS, RE_LINKS_2, MIN_LINE_LEN, MAX_LINE_LEN

    if "[deleted]" in line or "[Deleted]" in line or "[removed]" in line or "[Removed]" in line:
        return None

    if use_len:
        line_len = len(line)
        if line_len < MIN_LINE_LEN or line_len > MAX_LINE_LEN:
            return None

    line = fix_text(line, normalization='NFKC') # Normalization / text fixing
    line = re.sub(RE_R, '\n', line) # Strip \r
    if combine_lines:
        line = re.sub(RE_TABS_LINES, ' ', line) # Replace runs of whitespace and newlines with a single space
    else:
        line = re.sub(RE_TABS, ' ', line) # Replace runs of whitespace with a single space
        line = re.sub(RE_TRIM, r'\g<1>', line) # Trim any new lines
        line = re.sub(RE_LINES, '\n\n', line) # Replace runs of newlines with two newlines at max
    line = re.sub(RE_LINKS, r'\g<1>: <url>', line) # Replace links with a code
    line = re.sub(RE_LINKS_2, '<url>', line) # Replace links with a code
    line = re.sub(RE_LINKS_3, '<url>', line) # Replace links with a code
    line = line.strip()

    if use_len:
        line_len = len(line)
        if line_len < MIN_LINE_LEN or line_len > MAX_LINE_LEN:
            return None
    
    return line

def get_text(request):
    text = request.text
    if text is None:
        return None
    
    try:
        return ujson.loads(text)
    except ValueError:
        return None
    return None