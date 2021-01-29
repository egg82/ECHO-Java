import glob
import os
import shutil
import re
import ujson
from typing import Dict

from ftfy import fix_text
from tqdm import tqdm

import bz2
import lzma
import zstandard as zstd

from utils import parse_int
from utils import clean_line

from settings import MAX_OUT_FILE_SIZE
from settings import REDDIT_DATA_PATH
from settings import READ_SIZE
from settings import TRAINING_NAME
from settings import OUT_DIR
from settings import MIN_LINE_LEN
from settings import MAX_LINE_LEN

from settings import START_LINE_TOKEN
from settings import END_LINE_TOKEN
from settings import COMBINE_LINES

MAX_COMMENTS = 1000000

# https://github.com/pender/chatbot-rnn/blob/master/reddit-parse/parser_config_standard.json

BLACKLISTED_SUBREDDITS = None
WHITELISTED_SUBREDDITS = None
BLACKLISTED_WORDS = None

blk_s = []
# Minecraft
"""
wht_s = [
    "minecraft",
    "minecraftsuggestions",
    "competitiveminecraft",
    "minecraftrtx",
    "minecraftfaq",
    "minecrafthelp",
    "minecraftabnormals",
    "technicalminecraft",
    "minecraftconspiracies",
    "redstone",
    "redstonenoobs",
    "actualredstone",
    "survivalredstone",
    "mctourney",
    "flatcore",
    "mchardcore",
    "ultrahardcore",
    "minecraftfighters",
    "admincraft",
    "staffcraft",
    "spongeproject",
    "feedthebeast",
    "moddingmc",
    "minecraftmemes",
    "cursedminecraft",
    "minecrafthmm",
    "mcshowerthoughts",
    "minecraftcirclejerk",
    "crappymcsuggestions",
    "shittymcsuggestions",
    "shittymcbuilds",
    "cubeworldproblems",
    "minecraftirl"
]
"""
# Woke AF
"""
wht_s = [
    "feminism",
    "explainlikeimfive",
    "science",
    "accidentalrenaissance",
    "UnsolvedMysteries",
    "funfacts",
    "todayilearned",
    "pareidolia",
    "showerthoughts",
    "futurology",
    "nottheonion",
    "wokekids",
    "BlackLivesMatter"
]
"""
# Memelord
"""
wht_s = [
    "minecraftconspiracies",
    "birdsarentreal",
    "masterhacker",
    "iamverysmart",
    "thathappened",
    "im14andthisisdeep",
    "justneckbeardthings",
    "niceguys",
    "cringepics",
    "tumblrinaction",
    "fellowkids",
    "forwardsfromgrandma",
    "delusionalartists",
    "terriblefacebookmemes",
    "creepypms",
    "oddlysexual",
    "shittytechsupport",
    "shittyaskreddit",
    "shittytechprotips",
    "shittylifeprotips",
    "oneliners",
    "shitpost",
    "lewronggeneration",
    "whitepeoplefacebook",
    "4panelcringe",
    "whitepeopletwitter",
    "shittyscience",
    "holdmycosmo",
    "ifeelgoodtoday",
    "memes",
    "me_irl",
    "unexpected",
    "topmindsofreddit",
    "nottheonion",
    "wokekids",
    "outside",
    "madlads",
    "showerthoughts"
]
"""
# Memelord-Simple
"""
wht_s = [
    "minecraftconspiracies",
    "birdsarentreal",
    "masterhacker",
    "iamverysmart",
    "im14andthisisdeep",
    "cringepics",
    "forwardsfromgrandma",
    "delusionalartists",
    "shittytechsupport",
    "shittyaskreddit",
    "shittytechprotips",
    "shittylifeprotips",
    "shitpost",
    "whitepeopletwitter",
    "shittyscience",
    "holdmycosmo",
    "ifeelgoodtoday",
    "memes",
    "me_irl",
    "topmindsofreddit",
    "outside"
]
"""
# Tech stuff
"""
wht_s = [
    "ShittyTechSupport",
    "masterhacker",
    "techsupportgore",
    "sysadmin",
    "computertechs",
    "gaming",
    "pcmasterrace"
]
"""
# News/Politics
wht_s = [
    "worldnews",
    "news",
    "worldevents",
    "worldnews2",
    "RepublicOfPolitics",
    "LGBTnews",
    "politics2",

    "politics",
    "uspolitics",
    "AmericanPolitics",
    "AmericanGovernment"
]
blk_w = [
    "upvot",
    "downvot",
    "updoot",
    "downdoot",
    "ooc:",
    "u/",
    "subreddit",
    "post"
]

BLACKLISTED_SUBREDDITS = set(subreddit.strip().lower() for subreddit in blk_s)
WHITELISTED_SUBREDDITS = set(subreddit.strip().lower() for subreddit in wht_s)
BLACKLISTED_WORDS = set(word.strip().lower() for word in blk_w)

class Comment:
    def __init__(self, body):
        self._body = body
        self._children: Dict[str, str] = {}
    
    def get_body(self):
        return self._body
    
    def get_children(self):
        return self._children
    
    def add_child(self, comment_id, body):
        self._children[comment_id] = body

comments: Dict[str, Comment] = {}

def main():
    global REDDIT_DATA_PATH, OUT_DIR, comments

    print("Extracting files..")
    for file in glob.iglob(REDDIT_DATA_PATH + os.sep + "**" + os.sep + "*.bz2", recursive=True):
        extract_file(file)
    for file in glob.iglob(REDDIT_DATA_PATH + os.sep + "**" + os.sep + "*.xz", recursive=True):
        extract_file(file)
    for file in glob.iglob(REDDIT_DATA_PATH + os.sep + "**" + os.sep + "*.zst", recursive=True):
        extract_file(file)
    
    print("Parsing decompressed data..")
    if os.path.exists(OUT_DIR):
        shutil.rmtree(OUT_DIR)
    os.makedirs(OUT_DIR + os.sep + "raw")
    for file in glob.iglob(REDDIT_DATA_PATH + os.sep + "**" + os.sep + "*.raw", recursive=True):
        parse_raw_file(file, OUT_DIR + os.sep + "raw")
    
    print("Re-ordering parsed data..")
    files = glob.glob(OUT_DIR + os.sep + "raw" + os.sep + "*.dat")
    files.sort(key=os.path.getmtime)
    for file in files:
        parse_file(file)

    print("Dumping finalized data..")
    dump_data(comments, OUT_DIR + os.sep + "out.txt")

    print("Done!")

def extract_file(file: str):
    extracted = os.path.splitext(file)[0]
    root, ext = os.path.splitext(extracted)
    if not ext:
        extracted += ".raw"

    if os.path.exists(extracted) and os.path.isdir(extracted):
        shutil.rmtree(file)
    
    if os.path.exists(extracted):
        return
        
    print("Extracting " + file)
    if os.path.splitext(file)[1] == ".bz2":
        extract_bz2(file, extracted)
    if os.path.splitext(file)[1] == ".xz":
        extract_xz(file, extracted)
    if os.path.splitext(file)[1] == ".zst":
        extract_zstd(file, extracted)

def extract_bz2(inp: str, outp: str):
    global READ_SIZE

    with bz2.open(inp, "rb") as compressed, open(outp, "wb") as raw:
        for data in iter(lambda : compressed.read(READ_SIZE), b''):
            raw.write(data)

def extract_xz(inp: str, outp: str):
    with lzma.open(inp, "rb") as compressed, open(outp, "wb") as raw:
        for data in iter(lambda : compressed.read(READ_SIZE), b''):
            raw.write(data)

def extract_zstd(inp: str, outp: str):
    with open(inp, "rb") as file, open(outp, "wb") as raw:
        decompressor = zstd.ZstdDecompressor()
        with decompressor.stream_reader(file, read_size=READ_SIZE) as compressed:
            for data in iter(lambda : compressed.read(READ_SIZE), b''):
                raw.write(data)

comments_per_sub = {}

def parse_raw_file(in_file: str, out_dir: str):
    global BLACKLISTED_SUBREDDITS, WHITELISTED_SUBREDDITS, BLACKLISTED_WORDS, MIN_LINE_LEN, MAX_LINE_LEN, COMBINE_LINES, MAX_COMMENTS, comments_per_sub

    print("Parsing " + in_file)
    num_comments = 0
    
    use_blacklisted_subreddits = len(BLACKLISTED_SUBREDDITS) > 0
    use_whitelisted_subreddits = len(WHITELISTED_SUBREDDITS) > 0
    use_blacklisted_words = len(BLACKLISTED_WORDS) > 0

    if not use_blacklisted_subreddits and not use_whitelisted_subreddits:
        return

    comment_buffer = []

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

    with open(out_file, "at", encoding="utf8") as outp:
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
            for text in tqdm(inp, total=size, unit_scale=True):
                comment = None
                try:
                    comment = ujson.loads(text)
                except ValueError:
                    print("Failure to parse JSON: " + text)
                    continue

                body = comment["body"]

                body_len = len(body)
                if body_len < MIN_LINE_LEN or body_len > MAX_LINE_LEN:
                    continue

                subreddit = comment["subreddit"].lower()
                if use_blacklisted_subreddits and subreddit in BLACKLISTED_SUBREDDITS:
                    continue
                if use_whitelisted_subreddits and subreddit not in WHITELISTED_SUBREDDITS:
                    continue

                num_total_comments = comments_per_sub.get(subreddit, 0)

                body = clean_line(body, COMBINE_LINES)
                if body is None:
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

                comment_buffer.append(ujson.dumps(
                    {
                        "body": body,
                        "id": "t1_" + comment["id"],
                        "parent": comment["parent_id"] if comment["parent_id"].startswith("t1_") else ""
                    }
                )) # Faster than insert()
                num_comments += 1
                num_total_comments += 1
                comments_per_sub[subreddit] = num_total_comments
                if num_comments % 10000 == 0:
                    for b in comment_buffer:
                        outp.write(b + '\n')
                    comment_buffer *= 0 # Very fast clear()
                    #print("Wrote " + str(num_comments) + " comments")
                
                if num_total_comments >= MAX_COMMENTS:
                    print("\nReached MAX_COMMENTS limit for /r/" + subreddit + ", skipping new comments..")
                    if use_blacklisted_subreddits:
                        BLACKLISTED_SUBREDDITS.add(subreddit)
                    if use_whitelisted_subreddits:
                        WHITELISTED_SUBREDDITS.remove(subreddit)
                        if len(WHITELISTED_SUBREDDITS) == 0:
                            break
        if not num_comments % 10000 == 0:
            for b in comment_buffer:
                outp.write(b + '\n')
        if num_comments > 0:
            print("Wrote " + str(num_comments) + " comments")

def parse_file(in_file: str):
    global comments

    print("Gathering comments from " + in_file)
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
        for text in tqdm(inp, total=size, unit_scale=True):
            comment = None
            try:
                comment = ujson.loads(text)
            except ValueError:
                print("Failure to parse JSON: " + text)
                continue

            comment_id = comment["id"]
            parent = comment["parent"]
            if parent is None or len(parent) == 0:
                comments[comment_id] = Comment(comment["body"])
            else:
                if parent in comments:
                    comments[parent].add_child(comment_id, comment["body"])

def dump_data(data, out_file):
    global MAX_OUT_FILE_SIZE, START_LINE_TOKEN, END_LINE_TOKEN

    data_num = 0
    
    with open(out_file, "wt", encoding="utf8") as outp:
        for d in tqdm(data.values(), total=len(data), unit_scale=True):
            outp.write(START_LINE_TOKEN + d.get_body())
            if len(d.get_children()) > 0:
                outp.write('\n')
            com=[c for key,c in d.get_children().items()]
            for i in range(0, len(com) - 1):
                outp.write(com[i] + '\n')
            if len(com) > 0:
                outp.write(com[len(com) - 1])
            outp.write(END_LINE_TOKEN + '\n')
            data_num +=1
            if data_num % 10000 == 0:
                outp.flush()
                if os.stat(out_file).st_size > MAX_OUT_FILE_SIZE:
                    return

if __name__ == "__main__":
    main()