# coding=utf-8
import json

from learn.fmp.fmpValidateProcess import FmpValidateProcesser

# 获得json结构
# 获取自动化校验配置
# 自动测试接口

data_name = "CRM/data_bookCreat_CRM_904000001_cus_comp_sf.txt"
env = "http://10.239.20.237:8080"
change_key = ["serialId","bizOrderNo"]
# 生成数量
nums = 5

if __name__ == '__main__':
    processer = FmpValidateProcesser(env, "CRM")
    check_result = []
    param = json.loads(open(data_name).read())
    processer.change_key(change_key, param)
    try:
        api_result = processer.post_fmp_creat(param).json()
    except BaseException:
        print("服务未调用成功，请检查url:{}是否正确!".format(env))
        quit()
    if(api_result["code"]!=0):
        print("入参模板无法成功调用接口，请修改,错误结果：{}".format(api_result))
        quit()
    for i in range(1,nums):
        processer.change_key(change_key, param)
        api_result = processer.post_fmp_creat(param).json()
        if(api_result["code"]!=0):
            print("服务未调用成功：返回错误信息:{}".format(json.dumps(api_result)))
            quit()
        print(api_result)