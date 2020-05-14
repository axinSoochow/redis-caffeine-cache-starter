# coding=utf-8
import json

from learn.fmp.fmpValidateProcess import FmpValidateProcesser

# 获得json结构
# 获取自动化校验配置
# 自动测试接口

configId = "CRM/bookCreat_CRM_904000001_cus_comp_sf.txt"
data_name = "CRM/data_bookCreat_CRM_904000001_cus_comp_sf.txt"
env = "http://localhost:6666"
change_key = ["serialId","bizOrderNo"]
if __name__ == '__main__':
    txt = open(configId).read()
    config = json.loads(txt)
    processer = FmpValidateProcesser(env, "CRM")
    check_result = []
    param = json.loads(open(data_name).read())
    processer.change_key(change_key, param)
    api_result = processer.post_fmp_creat(param).json()
    if(api_result["code"]!=0):
        print("入参模板无法成功调用接口，请修改,错误结果：{}".format(api_result))
        quit()
    # 遍历校验单元
    for unit in config[FmpValidateProcesser.fields]:
        # 必填项自动测试
        print("开始——必填项{}自动测试".format(unit[processer.code]))
        processer.change_key(change_key, param)
        processer.require_auto_check(unit, param, check_result)

    print("自动测试结果{}:".format(check_result))
