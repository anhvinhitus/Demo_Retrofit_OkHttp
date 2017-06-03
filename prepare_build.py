#!/usr/local/bin/python3

import sys
import subprocess

def increase_version(file_path):
  import re
  re_pattern = 'versionCode\s+(?P<version>\d+)'
  re_pattern_versionName = 'versionName\s+"(?P<versionName>.*)"'
  new_content = ""

  versionCode = ""
  versionName = ""
  with open(file_path) as fin:
    s = fin.read()
    m = re.search(re_pattern, s)
    new_content = s
    if m is not None:
      version = int(m.group('version'))
      version = version + 1
      print("New version: %d" % version)
      new_content = re.sub(re_pattern, "versionCode %d" % version, s)
      versionCode = "%d" % version

    m = re.search(re_pattern_versionName, s)
    if m is not None:
      versionName = m.group('versionName')

  with open(file_path, 'wt') as fout:
    fout.write(new_content)

  return (versionCode, versionName)


def extract_gitlog():
  GIT_COMMIT_FIELDS = ['shortid', 'id', 'author_name', 'author_email', 'date', 'message']
  GIT_LOG_FORMAT = ['%h', '%H', '%an', '%ae', '%ad', '%s']

  GIT_LOG_FORMAT = '%x1f'.join(GIT_LOG_FORMAT) + '%x1e'

  p = subprocess.Popen('git log --format="%s"' % GIT_LOG_FORMAT, shell=True, stdout=subprocess.PIPE)
  (log, _) = p.communicate()
  # print(log)
  logstr = log.decode('utf8')
  # print(type(logstr), type(log))
  logstr = logstr.strip('\n\x1e').split("\x1e")
  logstr = [row.strip().split("\x1f") for row in logstr]
  logstr = [dict(zip(GIT_COMMIT_FIELDS, row)) for row in logstr]

  log_messages = []
  for ci in logstr:
    if 'Bump version' in ci['message']:
      break
    # add log messages
    log_messages.append(ci)

  return log_messages


def print_usage(valid_env):
  print('Usage: ./prepare_build.py [%s]\n' % '|'.join(valid_env))


def generate_release_notes(versionCode, versionName, release_notes_path):
  current_date = time.strftime('%Y-%m-%d %H:%M')
  # Zalo Pay (Sandbox v2.12.0 - build 253) - Released on 2017-05-18
  header = 'Zalo Pay (%s v%s - build %s) - Released on %s\n' % (env, versionName, versionCode, current_date)
  log_messages = extract_gitlog()

  log_format = ['+ %s by %s (#%s)' % (ci['message'], ci['author_name'], ci['shortid']) for ci in log_messages]

  with open(release_notes_path, 'wt') as fout:
    fout.write(header)
    fout.write('\n'.join(log_format))
    fout.write('\n')

def git_commit_bump_version(env, versionCode, versionName, files):
  # [Sandbox] Bump version 2.12.0 - build 253
  commit_message = "[%s] Bump version %s - build %s" % (env, versionName, versionCode)

  subprocess.run(["git", "add"] + files)
  subprocess.run(["git", "commit", "-m", commit_message])

import time

valid_env = ["Sandbox", "Staging", "Production"]

if len(sys.argv) < 2:
  print_usage(valid_env)
  exit(1)

env = sys.argv[1]
if env not in valid_env:
  print_usage(valid_env)
  exit(1)

app_gradle = 'ZaloPay/app/build.gradle'
release_notes = 'ci/release_notes.txt'

(versionCode, versionName) = increase_version(app_gradle)
generate_release_notes(versionCode, versionName, release_notes)
git_commit_bump_version(env, versionCode, versionName, [app_gradle, release_notes])


