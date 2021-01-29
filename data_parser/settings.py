import os

API_GRANULARITY_TRIES_MAX = 3
API_DATA_MAX = 25
API_DATA_MIN = 18

MIN_LINE_LEN = 4
MAX_LINE_LEN = 240

FAILURES_FILE = "failures.txt"
TIME_FILE = "current_time.txt"
REDDIT_DATA_PATH = "W:\\reddit_data"
MODEL_TYPE = "gpt2"

MAX_OUT_FILE_SIZE = 700 * 1024 * 1024 # TODO: 700MB limit
READ_SIZE = 8 * 1024 * 1024 # 4MB

TRAINING_NAME="horror"
CURRENT_PATH = os.path.dirname(os.path.realpath(__file__))
OUT_DIR = CURRENT_PATH + os.sep + "train" + os.sep + TRAINING_NAME

#START_LINE_TOKEN = "<|startoftext|>"
START_LINE_TOKEN = ""
#END_LINE_TOKEN = "<|endoftext|>"
END_LINE_TOKEN = ""
#COMBINE_LINES = False
COMBINE_LINES = True

GET_HEADERS = {
    "User-Agent": "egg82/ECHO",
    "Accept": "application/json",
    "Connection": "close",
    "Accept-Language": "en-US,en;q=0.8"
}

GOOGLE_PARAMS = {
    "safe": "medium"
}