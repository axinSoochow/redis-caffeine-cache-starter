import json
import time;

import requests
# 中台接入层模块
class FmpValidateProcesser(object):
    fields = "fields"
    code = "fieldCode"
    name = "fieldName"
    type = "fieldType"
    enum_list = "enumList"
    require = "require"

    biz_error = 3020005

    # 默认本地地址
    env = "http://localhost:6666"
    creat_path = "/api/v2/book/create"
    update_path = "/api/v2/book/update"
    # 来源
    source = None

    def __init__(self, env, source):
        self.env = env
        self.source = source

    # post请求
    def post_fmp_creat(self, data):
        return requests.post(self.env + self.creat_path, data=json.dumps(data),
                             headers={"X-OAuth-Client": self.source, "Content-Type": "application/json;charset=UTF-8"})

    # 自动测试必填项
    def require_auto_check(self, unit, data, result):
        if (unit[self.require] == False):
            return
        temp = data[unit[self.code]]
        del data[unit[self.code]]
        api_result = self.post_fmp_creat(data).json()
        if (api_result["code"] != self.biz_error):

            result.append("{}是必填字段，在没有传该字段时，系统出现预期外的结果:{}".format(unit[self.code],api_result))
        data[unit[self.code]] = temp

    # ——————————————————————— 工具方法 ——————————————————————————
    def change_key(self, change_key, param):
        new_key= "axin_{}".format(int(round(time.time() * 1000)))
        for key in change_key:
            del param[key]
            param[key] = new_key

if __name__ == '__main__':
    print("axin_{}".format(int(round(time.time() * 1000))))