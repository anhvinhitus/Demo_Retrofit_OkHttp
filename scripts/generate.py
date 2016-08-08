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
                    header[0]:row[0],
                    header[1]:row[1],
                    header[2]:row[2]
                }
                data.append(obj)
                # print(row[0], row[2], row[1])
            rownum += 1
    return data


def convert_action(data):
    for ev in data:
        ev['Action_UPPER'] = ev['Action'].upper()


def print_zpevents(data, f1, f2):
    lines = []
    for ev in data:
        lines.append('    public static final int %s = %s;' % (ev['Action_UPPER'], ev['EventId']))
    buffer = '''package vn.com.vng.zalopay.analytics;

/**
 * Auto-generated
 */
public class ZPEvents {
%s

%s

%s
}

''' % ('\n'.join(lines), f1, f2)
    return buffer


def print_convert_action(data):
    lines = []
    for ev in data:
        lines.append('            case %s:\n                return "%s";' % (ev['Action_UPPER'], ev['Action']))
    buffer = '''    public static String actionFromEventId(int eventId) {
        switch (eventId) {
%s
            default:
                return "DefaultAction";
        }
    }''' % '\n'.join(lines)
    return buffer


def print_convert_category(data):
    lines = []
    for ev in data:
        lines.append('            case %s:\n                return "%s";' % (ev['Action_UPPER'], ev['Category']))
    buffer = '''    public static String categoryFromEventId(int eventId) {
        switch (eventId) {
%s
            default:
                return "DefaultCategory";
        }
    }''' % '\n'.join(lines)
    return buffer



def write_file(file_path, buffer):
    print("Updating %s" % file_path)
    with open(file_path, 'w') as etd:
        etd.write(buffer)


data = read_csv('ZaloPay-EventAnalytics-Data.csv')
convert_action(data)
# print(data)

ac = print_convert_action(data)
# print(ac)

cat = print_convert_category(data)
# print(cat)

en = print_zpevents(data, ac, cat)
print(en)
write_file('../maven_src/zalopayanalytics/src/main/java/vn/com/vng/zalopay/analytics/ZPEvents.java', en)

print("Done!")
