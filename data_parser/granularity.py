from enum import Enum

class Granularity(Enum):
    YEARLY = 3.154e+7
    HALF_YEARLY = 1.577e+7
    TRI_MONTHLY = 7.884e+6
    MONTHLY = 2.628e+6
    TRI_WEEKLY = 1.814e+6
    BI_WEEKLY = 1.21e+6
    WEEKLY = 604800
    QUINT_DAILY = 432000
    TRI_DAILY = 259200
    DAILY = 86400
    HALF_DAILY = 43200
    QUARTER_DAILY = 21600
    HOURLY = 3600
    HALF_HOURLY = 1800
    QUARTER_HOURLY = 900
    FIVE_MINS = 300
    ONE_MIN = 60
    HALF_MIN = 30
    QUARTER_MIN = 15

def decrease_granularity(granularity):
    if granularity == Granularity.QUARTER_MIN.value:
        return int(Granularity.HALF_MIN.value)
    elif granularity == Granularity.HALF_MIN.value:
        return int(Granularity.ONE_MIN.value)
    elif granularity == Granularity.ONE_MIN.value:
        return int(Granularity.FIVE_MINS.value)
    elif granularity == Granularity.FIVE_MINS.value:
        return int(Granularity.QUARTER_HOURLY.value)
    elif granularity == Granularity.QUARTER_HOURLY.value:
        return int(Granularity.HALF_HOURLY.value)
    elif granularity == Granularity.HALF_HOURLY.value:
        return int(Granularity.HOURLY.value)
    elif granularity == Granularity.HOURLY.value:
        return int(Granularity.QUARTER_DAILY.value)
    elif granularity == Granularity.QUARTER_DAILY.value:
        return int(Granularity.HALF_DAILY.value)
    elif granularity == Granularity.HALF_DAILY.value:
        return int(Granularity.DAILY.value)
    elif granularity == Granularity.DAILY.value:
        return int(Granularity.TRI_DAILY.value)
    elif granularity == Granularity.TRI_DAILY.value:
        return int(Granularity.QUINT_DAILY.value)
    elif granularity == Granularity.QUINT_DAILY.value:
        return int(Granularity.WEEKLY.value)
    elif granularity == Granularity.WEEKLY.value:
        return int(Granularity.BI_WEEKLY.value)
    elif granularity == Granularity.BI_WEEKLY.value:
        return int(Granularity.TRI_WEEKLY.value)
    elif granularity == Granularity.TRI_WEEKLY.value:
        return int(Granularity.MONTHLY.value)
    elif granularity == Granularity.MONTHLY.value:
        return int(Granularity.TRI_MONTHLY.value)
    elif granularity == Granularity.TRI_MONTHLY.value:
        return int(Granularity.HALF_YEARLY.value)
    return int(Granularity.YEARLY.value)

def increase_granularity(granularity):
    if granularity == Granularity.YEARLY.value:
        return int(Granularity.HALF_YEARLY.value)
    elif granularity == Granularity.HALF_YEARLY.value:
        return int(Granularity.TRI_MONTHLY.value)
    elif granularity == Granularity.TRI_MONTHLY.value:
        return int(Granularity.MONTHLY.value)
    elif granularity == Granularity.MONTHLY.value:
        return int(Granularity.TRI_WEEKLY.value)
    elif granularity == Granularity.TRI_WEEKLY.value:
        return int(Granularity.BI_WEEKLY.value)
    elif granularity == Granularity.BI_WEEKLY.value:
        return int(Granularity.WEEKLY.value)
    elif granularity == Granularity.WEEKLY.value:
        return int(Granularity.QUINT_DAILY.value)
    elif granularity == Granularity.QUINT_DAILY.value:
        return int(Granularity.TRI_DAILY.value)
    elif granularity == Granularity.TRI_DAILY.value:
        return int(Granularity.DAILY.value)
    elif granularity == Granularity.DAILY.value:
        return int(Granularity.HALF_DAILY.value)
    elif granularity == Granularity.HALF_DAILY.value:
        return int(Granularity.QUARTER_DAILY.value)
    elif granularity == Granularity.QUARTER_DAILY.value:
        return int(Granularity.HOURLY.value)
    elif granularity == Granularity.HOURLY.value:
        return int(Granularity.HALF_HOURLY.value)
    elif granularity == Granularity.HALF_HOURLY.value:
        return int(Granularity.QUARTER_HOURLY.value)
    elif granularity == Granularity.QUARTER_HOURLY.value:
        return int(Granularity.FIVE_MINS.value)
    elif granularity == Granularity.FIVE_MINS.value:
        return int(Granularity.ONE_MIN.value)
    elif granularity == Granularity.ONE_MIN.value:
        return int(Granularity.HALF_MIN.value)
    return int(Granularity.QUARTER_MIN.value)