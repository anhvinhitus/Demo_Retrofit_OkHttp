# https://sandbox.zalopay.com.vn/v001/tpe/v001getplatforminfo?accesstoken=iCHL2EHudZ5arFL7s3/lBqf%2BCcVrKKb7g4w%2Bf65QfKY%3D.1497255534276.1497860274276&appid=1&appversion=2.14.0&dscreentype=iphone2x&mno=45201&platformcode=ios&sdkversion=3.4.0&userid=160525000004004

from termcolor import colored
import json, requests
import os
import sys
from urllib.parse import urljoin
from pick import pick
from common import check_url, suffixs, choose_environment

rsversion = '20170612.001'

real_url = 'https://zalopay.com.vn'
stg_url = 'https://stg.zalopay.com.vn'
sandbox_url = 'https://sandbox.zalopay.com.vn'

path = '/v001/tpe/v001getplatforminfo'
app_version = '2.14.0'
accesstoken = 'iCHL2EHudZ5arFL7s3/lBqf%2BCcVrKKb7g4w%2Bf65QfKY%3D.1497255534276.1497860274276'
userid = '160525000004004'
sdkversion = '3.4.0'

def requestSdkResource(dscreen_type, plat_form):
  new_url = '%s?accesstoken=%s' %(url, accesstoken)
  params = dict(
    # accesstoken = accesstoken,
    appversion = app_version,
    dscreentype = dscreen_type,
    platformcode = plat_form,
    appid = 1,
    mno = 45201,
    userid = userid,
    sdkversion = sdkversion,
  )
  resp = requests.get(url=new_url, params=params)
  data = resp.json()
  if data['returncode'] != 1:
    return (False, "Server return error %d" % data['returncode']) 
  resource = data['resource']
  if resource['rsversion'] == rsversion:
    (result, message) = check_url(resource['rsurl'])
    if not result:
      return (result, message)
  return (True, "")

#Choose environment
option = choose_environment()
print (('Environment: %s' %option))
if option == 'Real':
  base_url = real_url
elif option == 'Staging':
  base_url = stg_url
else:
  base_url = sandbox_url

url = urljoin(base_url, path)

print ('RsVersion require: %s' %rsversion)

for (screen_type, ostype) in suffixs():
  print('Checking for screen type %s on %s ...' % (screen_type, ostype))
  (result, message) = requestSdkResource(screen_type, ostype)
  if result:
    print(colored('VALID %s' % message, 'green'))
  else:
    print(colored('INVALID with message: %s' % message, 'red'))

