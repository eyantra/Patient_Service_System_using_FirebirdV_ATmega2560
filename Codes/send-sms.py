#!/usr/bin/python
import sys
__author__ = """
NAME: Abhijeet Rastogi (shadyabhi)
Profile: http://www.google.com/profiles/abhijeet.1989
"""

import cookielib
import urllib2
from getpass import getpass
import sys

url = 'http://www.mycantos.com/'

#Credentials taken here
username = "embeddedproject"
passwd = "madanrocks"

data = 'username='+username+'&password='+passwd+'&checklogin=1'
cj = cookielib.CookieJar()
opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cj))
try:
    usock = opener.open(url, data)
except IOError:
    print "Error fetching page www.mycantos.com\nExiting now.."
    sys.exit()

#Headers added to avoid the Missing data, try again!! error
opener.addheaders = [('Referer','http://www.mycantos.com/sendSMS.php'),('User-Agent','Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.3) Gecko/20091020 Ubuntu/9.10 (karmic) Firefox/3.5.3 GTB7.0')]

message =sys.argv[2:]
ms = ' '.join(message)
number = sys.argv[1] #"9769302931"
#number = "9423694632"
data_to_send = 'checkSMS=1&SMSnumber='+number+'&SMSmessage='+ms
url_send = "http://www.mycantos.com/sendSMStoanyone.php"

#SMS send POST
try:
    send = opener.open(url_send,data_to_send)
except IOError:
    print "Error sending SMS\nExiting now.."
    sys.exit()

print "SMS SENT!!!"
