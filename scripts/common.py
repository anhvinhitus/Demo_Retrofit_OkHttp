import json, requests
from pick import pick

def yes_no(answer):
  yes = set(['yes','y', 'ye', ''])
  no = set(['no','n'])
     
  while True:
    choice = raw_input(answer).lower()
    if choice in yes:
      return True
    elif choice in no:
      return False
    else:
      print ("Please respond with 'yes' or 'no'\n")

def check_url(url):
  r = requests.head(url)
  if r.status_code != 200:
    return (False, "URL %s has problem on server, HTTP code: %d" % (url, r.status_code))

  return (True, "")

def suffixs():
  return [
    ('hdpi', 'android'),
    ('xhdpi', 'android'),
    ('xxhdpi', 'android'),
    ('ipad1x', 'ios'),
    ('ipad2x', 'ios'),
    ('iphone1x', 'ios'),
    ('iphone2x', 'ios'),
    ('iphone3x', 'ios')
  ]
def choose_environment():
  options = ['Sandbox', 'Staging', 'Real']
  option, index = pick(options, 'Please choose environment: ')
  return option
