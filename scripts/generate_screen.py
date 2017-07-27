import os
import csv

def read_csv(file_path):
    data = []
    with open(file_path, 'rb') as csvfile:
        event_reader = csv.reader(csvfile, delimiter=',', quotechar='|')
        rownum = 0
        for row in event_reader:
            if rownum == 0:
                header = row
            else:
                obj = {
                    header[0]:row[0]
                }
                data.append(obj)
               # print(row[0])
            rownum += 1
    return data


def convert_screen(data):
    for ev in data:
        temp = ev['Screen Name'].upper().replace('][', '_')
        temp = temp.replace('[','').replace(']','')
        ev['Screens_UPPER'] = temp;

def print_zpscreens(data):
    lines = []
    for ev in data:
        lines.append('    public static final String %s = "%s";' % (ev['Screens_UPPER'], ev['Screen Name']))
    buffer = '''package vn.com.zalopay.analytics;

/**
 * Auto-generated
 */
public class ZPScreens {
%s

}

''' % ('\n'.join(lines))
    return buffer