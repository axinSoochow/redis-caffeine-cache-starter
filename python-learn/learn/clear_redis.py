# coding=utf-8
import sys
sys.path.append("/usr/local/lib/python3.7/site-packages")
import redis

# beta环境
# host = "m-redis-k8s.shbeta.ke.com"
# port = "36379"
# db = 2
# pwd = None
# keys_file = 'keys.txt'

# fmp-off
# host = "m-redis-k8s.shoff.ke.com"
# port = "38379"
# pwd = None
# db = 3
# keys_file = 'keys-fmp.txt'

# bfa-生产环境
# host = "m11507.mars.redis.ljnode.com"
# port = "11507"
# pwd = None
# db = None
# keys_file = 'keys-bfa.txt'

# fmp-生产环境
host = "m11982.zeus.redis.ljnode.com"
port = "11982"
pwd = "Hu9hAbGcqiVC6vYd"
db = None
keys_file = 'keys-fmp.txt'

if __name__ == '__main__':
    if pwd is not None:
        conn = redis.Redis(host=host, port=port, db=db, password=pwd)
    else:
        conn = redis.Redis(host=host, port=port, db=db)
    lines = open(keys_file).read().splitlines()
    print(lines)
    for line in lines:
        cursor = 0
        while True:
            keys = conn.scan(cursor, line, 1000)
            for key in keys[1]:
                print('%s被移除' % key.decode('utf-8'))
                conn.delete(key)
            if keys[0] == 0:
                break
            cursor = keys[0]
