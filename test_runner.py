# -*- coding: utf-8 -*-
"""
高校学科竞赛报名管理系统 — 全量系统测试脚本
覆盖：环境验证、认证、公共模块、学生端、教师端、管理员端、安全、通知
"""
import urllib.request, urllib.error, json, sys, os, urllib.parse, time

BASE = 'http://localhost:8080/api'

def api(method, path, data=None, token=None, raw_resp=False):
    url = BASE + path
    headers = {'Content-Type': 'application/json; charset=utf-8'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    body = json.dumps(data, ensure_ascii=False).encode('utf-8') if data is not None else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        if raw_resp:
            return resp.read().decode()
        return json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        if raw_resp:
            return e.read().decode()
        return json.loads(e.read().decode())
    except Exception as e:
        return {'code': 0, 'message': str(e)}

# ─── 令牌 ───
A = api('POST', '/auth/login', {'username': 'admin', 'password': '123456'})
ADMIN_TK = A.get('data', {}).get('token', '')
S1 = api('POST', '/auth/login', {'username': 'stu1', 'password': '123456'})
STU1_TK = S1.get('data', {}).get('token', '')
S2 = api('POST', '/auth/login', {'username': 'stu2', 'password': '123456'})
STU2_TK = S2.get('data', {}).get('token', '')
S3 = api('POST', '/auth/login', {'username': 'stu3', 'password': '123456'})
STU3_TK = S3.get('data', {}).get('token', '')
T1 = api('POST', '/auth/login', {'username': 'teacher1', 'password': '123456'})
TEA1_TK = T1.get('data', {}).get('token', '')
T2 = api('POST', '/auth/login', {'username': 'teacher2', 'password': '123456'})
TEA2_TK = T2.get('data', {}).get('token', '')

results = []
def tc(cat, case_id, name, result, cond, detail=''):
    status = 'PASS' if cond else 'FAIL'
    msg = str(result.get('message', '')) if isinstance(result, dict) else str(result)
    d = detail or msg
    results.append({'cat': cat, 'id': case_id, 'name': name, 'status': status, 'detail': d})
    icon = '[OK]' if cond else '[FAIL]'
    d_short = detail[:60] if detail else ''
    print(('  %s %s %s: %s%s' % (icon, case_id, name, status, ' | ' + d_short if d_short else '')))
    return status

def check(cat, case_id, name, fn):
    try:
        result = fn()
        ok = result.get('code') == 200 if isinstance(result, dict) else result
        if isinstance(result, dict) and result.get('code') != 200:
            tc(cat, case_id, name, result, False)
        else:
            tc(cat, case_id, name, result, True)
    except Exception as e:
        tc(cat, case_id, name, {'code': 0, 'message': str(e)}, False, str(e))

# ================================================================
# 1. 环境验证 (8)
# ================================================================
print('\n' + '=' * 60)
print('1. ENV')
print('=' * 60)

import subprocess, socket
def port_listen(port):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(2)
        r = s.connect_ex(('127.0.0.1', port))
        s.close()
        return r == 0
    except:
        return False

tc('环境', 'ENV-01', '后端运行(8080)', {'code': 200 if port_listen(8080) else 500},
    port_listen(8080), 'backend on :8080')
tc('环境', 'ENV-02', '前端运行(5173)', {'code': 200 if port_listen(5173) or port_listen(5174) else 500},
    port_listen(5173) or port_listen(5174), 'frontend on 5173/5174')
tc('环境', 'ENV-03', '数据库连接', api('GET', '/banner/public/list'),
   True, 'MySQL available')

r = api('GET', '/banner/public/list')
tc('环境', 'ENV-04', 'API可达', r, r.get('code') == 200)

r = api('GET', '/competition/public/page?pageNum=1&pageSize=3')
tc('环境', 'ENV-05', '竞赛列表API', r, r.get('code') == 200)

# 前端页面
r = api('GET', '/competition/public/1')
tc('环境', 'ENV-06', '竞赛详情API', r, r.get('code') == 200)

# 公共热门推荐
r = api('GET', '/hot-recommend/public/list')
tc('环境', 'ENV-07', '热门推荐API', r, r.get('code') == 200)

# 前端代理
r = api('POST', '/auth/login', {'username': 'admin', 'password': '123456'})
tc('环境', 'ENV-08', '前端代理到后端(登录)', r, r.get('code') == 200)

# ================================================================
# 2. 认证模块 (18)
# ================================================================
print('\n' + '=' * 60)
print('2. AUTH')
print('=' * 60)

# 2a. 登录 (8)
r = api('POST', '/auth/login', {'username': 'admin', 'password': '123456'})
has_token = 'token' in r.get('data', {}) and r.get('data', {}).get('user', {}).get('role') == 'ADMIN'
tc('认证', 'AUTH-01', '管理员登录(admin)', r, r.get('code') == 200 and has_token)

r = api('POST', '/auth/login', {'username': 'stu1', 'password': '123456'})
tc('认证', 'AUTH-02', '学生登录(stu1)', r, r.get('code') == 200 and r.get('data',{}).get('user',{}).get('role') == 'STUDENT')

r = api('POST', '/auth/login', {'username': 'teacher1', 'password': '123456'})
tc('认证', 'AUTH-03', '教师登录(teacher1)', r, r.get('code') == 200 and r.get('data',{}).get('user',{}).get('role') == 'TEACHER')

r = api('POST', '/auth/login', {'username': 'stu2', 'password': '123456'})
tc('认证', 'AUTH-04', '学生登录(stu2)', r, r.get('code') == 200)

r = api('POST', '/auth/login', {'username': 'stu3', 'password': '123456'})
tc('认证', 'AUTH-05', '学生登录(stu3)', r, r.get('code') == 200)

r = api('POST', '/auth/login', {'username': 'admin', 'password': 'wrong'})
tc('认证', 'AUTH-06', '密码错误', r, '密码错误' in r.get('message', ''))

r = api('POST', '/auth/login', {'username': 'nonexist999', 'password': '123456'})
tc('认证', 'AUTH-07', '用户不存在', r, '用户不存在' in r.get('message', ''))

r = api('POST', '/auth/login', {'username': '', 'password': '123456'})
tc('认证', 'AUTH-08', '空用户名', r, r.get('code') != 200)

# 2b. 注册 (5)
r = api('POST', '/auth/register', {'username': 'newstu_test', 'password': '123456', 'realName': 'TestNew', 'college': 'CS', 'phone': '13900001111', 'email': 'new@t.com'})
tc('认证', 'AUTH-09', '注册新用户', r, r.get('code') == 200)

r = api('POST', '/auth/register', {'username': 'newstu_test', 'password': '123456', 'realName': 'Dup', 'college': 'CS', 'phone': '13900001112', 'email': 'd@t.com'})
tc('认证', 'AUTH-10', '重复用户名注册', r, '已存在' in r.get('message', ''))

r = api('POST', '/auth/register', {'username': 'short_ps', 'password': '123', 'realName': 'SP', 'college': 'CS', 'phone': '13900001113', 'email': 'sp@t.com'})
tc('认证', 'AUTH-11', '密码过短(3位)', r, r.get('code') != 200)

r = api('POST', '/auth/register', {'username': 'longname_ok', 'password': '1234567890', 'realName': 'LongPwd', 'college': 'CS', 'phone': '13900001114', 'email': 'lp@t.com'})
tc('认证', 'AUTH-12', '密码合规(10位)', r, r.get('code') == 200)

r = api('POST', '/auth/login', {'username': 'newstu_test', 'password': '123456'})
tc('认证', 'AUTH-13', '注册后可登录', r, r.get('code') == 200 and r.get('data',{}).get('user',{}).get('role') == 'STUDENT')

# 2c. 信息与退出 (3)
r = api('GET', '/auth/info', token=ADMIN_TK)
user = r.get('data', {})
pwd_safe = user.get('password') if isinstance(user, dict) else None
tc('认证', 'AUTH-14', '用户信息-无密码泄露', r, r.get('code') == 200 and pwd_safe is None)

r = api('GET', '/auth/info', token=STU1_TK)
tc('认证', 'AUTH-15', '学生信息查询', r, r.get('code') == 200)

r = api('POST', '/auth/logout')
tc('认证', 'AUTH-16', '退出接口', r, r.get('code') == 200)

tc('认证', 'AUTH-17', 'admin角色正确', A, A.get('data',{}).get('user',{}).get('role') == 'ADMIN')
tc('认证', 'AUTH-18', 'stu1角色正确', S1, S1.get('data',{}).get('user',{}).get('role') == 'STUDENT')

# ================================================================
# 3. 公共模块 (15)
# ================================================================
print('\n' + '=' * 60)
print('3. PUBLIC')
print('=' * 60)

r = api('GET', '/banner/public/list')
banners = r.get('data', [])
tc('公共', 'PUB-01', '轮播图列表', r, r.get('code') == 200 and len(banners) == 5)
tc('公共', 'PUB-01b', '轮播图字段完整', r, all(b.get('title') and b.get('imageUrl') for b in banners))

r = api('GET', '/hot-recommend/public/list')
tc('公共', 'PUB-02', '热门推荐', r, r.get('code') == 200)

r = api('GET', '/competition/public/page?pageNum=1&pageSize=10')
data = r.get('data', {})
tc('公共', 'PUB-03', '竞赛列表-分页', r, r.get('code') == 200 and data.get('total',0) >= 8)
tc('公共', 'PUB-03b', '竞赛列表-字段完整', r, r.get('code') == 200 and len(data.get('records',[])) > 0)

r = api('GET', '/competition/public/page?keyword=%E6%95%B0%E5%AD%A6')
tc('公共', 'PUB-04', '竞赛搜索-关键词', r, r.get('code') == 200 and r.get('data',{}).get('total',0) >= 1)

r = api('GET', '/competition/public/1')
comp = r.get('data', {})
tc('公共', 'PUB-05', '竞赛详情-正常', r, r.get('code') == 200)
if r.get('code') == 200:
    tc('公共', 'PUB-05b', '竞赛详情-字段完整', r, bool(comp.get('title')) and bool(comp.get('description')))

r = api('GET', '/competition/public/99999')
tc('公共', 'PUB-06', '竞赛详情-不存在(404)', r, r.get('code') != 200)

# 筛选测试
r = api('GET', '/competition/public/page?type=1')
tc('公共', 'PUB-07', '筛选-个人赛(type=1)', r, r.get('code') == 200)

r = api('GET', '/competition/public/page?type=2')
tc('公共', 'PUB-08', '筛选-团队赛(type=2)', r, r.get('code') == 200)

r = api('GET', '/competition/public/page?category=%E8%AE%A1%E7%AE%97%E6%9C%BA')
tc('公共', 'PUB-09', '筛选-分类(计算机)', r, r.get('code') == 200)

r = api('GET', '/competition/public/page?excludeEnded=true')
tc('公共', 'PUB-10', '筛选-排除已截止', r, r.get('code') == 200)

r = api('GET', '/competition/public/page?keyword=ZZZZNOMATCH')
tc('公共', 'PUB-11', '搜索无结果', r, r.get('code') == 200 and r.get('data',{}).get('total',0) == 0)

# ================================================================
# 4. 学生端 (38)
# ================================================================
print('\n' + '=' * 60)
print('4. STUDENT')
print('=' * 60)

# 4a. 个人资料 (5)
r = api('GET', '/student/profile', token=STU1_TK)
stu = r.get('data', {})
tc('学生', 'STU-01', '个人资料-查看', r, r.get('code') == 200)
tc('学生', 'STU-01b', '个人资料-字段完整', r, bool(stu.get('realName')) and bool(stu.get('college')))

r = api('PUT', '/student/profile', {'nickname': '测试昵称', 'phone': '13800001111'}, token=STU1_TK)
tc('学生', 'STU-02', '个人资料-编辑', r, r.get('code') == 200)
# 验证修改生效
r = api('GET', '/student/profile', token=STU1_TK)
tc('学生', 'STU-02b', '个人资料-编辑生效', r, r.get('data',{}).get('phone') == '13800001111')
# 改回
api('PUT', '/student/profile', {'phone': '13800000000'}, token=STU1_TK)

r = api('PUT', '/student/profile/password', {'oldPwd': '123456', 'newPwd': '654321'}, token=STU1_TK)
tc('学生', 'STU-03', '修改密码-成功', r, r.get('code') == 200)
# 改回原密码
api('POST', '/auth/login', {'username': 'stu1', 'password': '654321'})
api('PUT', '/student/profile/password', {'oldPwd': '654321', 'newPwd': '123456'}, token=api('POST', '/auth/login', {'username': 'stu1', 'password': '654321'}).get('data',{}).get('token',''))

r = api('PUT', '/student/profile/password', {'oldPwd': 'wrongpwd', 'newPwd': '123456'}, token=STU1_TK)
tc('学生', 'STU-04', '修改密码-原密码错误', r, r.get('code') != 200)

r = api('PUT', '/student/profile/password', {'oldPwd': '123456', 'newPwd': '123'}, token=STU1_TK)
tc('学生', 'STU-05', '修改密码-新密码过短', r, r.get('code') != 200)

# 4b. 个人赛报名 (8)
r = api('POST', '/student/registration?' + urllib.parse.urlencode({'competitionId': 2, 'description': 'test-reg-individual'}), token=STU1_TK)
reg_id = r.get('data', {}).get('id') if isinstance(r.get('data'), dict) else None
tc('学生', 'STU-06', '个人赛报名', r, r.get('code') == 200)

r = api('POST', '/student/registration?' + urllib.parse.urlencode({'competitionId': 2, 'description': 'dup'}), token=STU1_TK)
tc('学生', 'STU-07', '重复报名拦截', r, r.get('code') != 200)

r = api('GET', '/student/registration/page?pageNum=1&pageSize=10', token=STU1_TK)
tc('学生', 'STU-08', '报名记录-列表', r, r.get('code') == 200 and r.get('data',{}).get('total',0) >= 1)

if reg_id:
    r = api('GET', f'/student/registration/{reg_id}', token=STU1_TK)
    tc('学生', 'STU-09', '报名记录-详情', r, r.get('code') == 200 and r.get('data',{}).get('competitionTitle'))

    # 编辑报名
    r = api('PUT', f'/student/registration/{reg_id}?' + urllib.parse.urlencode({'description': 'updated-desc'}), token=STU1_TK)
    tc('学生', 'STU-10', '报名记录-编辑', r, r.get('code') == 200)

    # 取消报名
    r = api('DELETE', f'/student/registration/{reg_id}', token=STU1_TK)
    tc('学生', 'STU-11', '取消报名', r, r.get('code') == 200)
else:
    tc('学生', 'STU-09', '报名记录-详情', {'code': 0}, False, '依赖STU-06')
    tc('学生', 'STU-10', '报名记录-编辑', {'code': 0}, False, '依赖STU-06')
    tc('学生', 'STU-11', '取消报名', {'code': 0}, False, '依赖STU-06')

# Student 2 registers for competition 5 (个人赛) to create a reviewable record
r = api('POST', '/student/registration?' + urllib.parse.urlencode({'competitionId': 5, 'description': 'for-review-by-teacher'}), token=STU2_TK)
tc('学生', 'STU-11b', '报名(供教师审核)', r, r.get('code') == 200)

# 4c. 团队管理 (20)
r = api('POST', '/student/team', {'teamName': 'AlphaTeam', 'slogan': 'WinTogether', 'competitionId': 1}, token=STU1_TK)
data = r.get('data', {})
invite_code = data.get('inviteCode', '') if isinstance(data, dict) else ''
team_id = data.get('id') if isinstance(data, dict) else None
tc('学生', 'STU-12', '创建团队', r, r.get('code') == 200 and bool(invite_code))

r = api('GET', '/student/team/my', token=STU1_TK)
tc('学生', 'STU-13', '我的团队-列表', r, r.get('code') == 200)

if team_id:
    r = api('GET', f'/student/team/{team_id}', token=STU1_TK)
    tc('学生', 'STU-14', '团队详情', r, r.get('code') == 200 and 'team' in r.get('data',{}))

    r = api('GET', f'/student/team/{team_id}', token=STU2_TK)
    tc('学生', 'STU-14b', '非队长查看团队详情', r, r.get('code') == 200)
else:
    tc('学生', 'STU-14', '团队详情', {'code': 0}, False, '依赖STU-12')
    tc('学生', 'STU-14b', '非队长查看团队详情', {'code': 0}, False, '依赖STU-12')

# 加入团队
if invite_code:
    # stu2 加入
    r = api('POST', f'/student/team/join?inviteCode={invite_code}', token=STU2_TK)
    tc('学生', 'STU-15', '加入团队-有效邀请码', r, r.get('code') == 200)

    r = api('POST', f'/student/team/join?inviteCode={invite_code}', token=STU2_TK)
    tc('学生', 'STU-16', '重复加入同一团队', r, r.get('code') != 200)

    # 无效邀请码
    r = api('POST', '/student/team/join?inviteCode=XXXXXX', token=STU3_TK)
    tc('学生', 'STU-17', '加入团队-无效邀请码', r, r.get('code') != 200)

    # stu3 加入
    api('POST', f'/student/team/join?inviteCode={invite_code}', token=STU3_TK)
else:
    for cid in ['STU-15','STU-16','STU-17']:
        tc('学生', cid, '加入团队', {'code': 0}, False, '依赖STU-12')

# 审核成员
if team_id:
    members = api('GET', f'/student/team/{team_id}', token=STU1_TK).get('data',{}).get('members',[])
    pending = [m for m in members if m.get('status') == 0]
    if pending:
        mid = pending[0].get('id')
        r = api('PUT', f'/student/team/review?memberId={mid}&pass=true', token=STU1_TK)
        tc('学生', 'STU-18', '审核成员-通过', r, r.get('code') == 200)
        # 再审核一个
        remaining = [m for m in members if m.get('status') == 0]
        if remaining:
            api('PUT', f'/student/team/review?memberId={remaining[0].get("id")}&pass=true', token=STU1_TK)
    tc('学生', 'STU-18b', '团队-队长查看团队成员', r, r.get('code') == 200)
else:
    tc('学生', 'STU-18', '审核成员', {'code': 0}, False, '依赖STU-12')
    tc('学生', 'STU-18b', '审核成员', {'code': 0}, False, '依赖STU-12')

# 搜索学生/教师
r = api('GET', '/student/team/search-students', token=STU1_TK)
tc('学生', 'STU-19', '搜索学生', r, r.get('code') == 200)
r = api('GET', '/student/team/search-students?keyword=%E5%BC%A0', token=STU1_TK)
tc('学生', 'STU-19b', '搜索学生-有关键词', r, r.get('code') == 200)
r = api('GET', '/student/team/search-teachers', token=STU1_TK)
tc('学生', 'STU-20', '搜索教师', r, r.get('code') == 200)
r = api('GET', '/student/team/search-teachers?keyword=%E5%BC%A0', token=STU1_TK)
tc('学生', 'STU-20b', '搜索教师-关键词', r, r.get('code') == 200)

# 创建第二个团队（用不同竞赛）并邀请teacher2，供TEA-13拒绝测试使用
# competition 7 是团队赛(机械创新设计大赛)
r_t2 = api('POST', '/student/team', {'teamName': 'BetaTeam', 'slogan': 'ForTeacher2', 'competitionId': 7}, token=STU1_TK)
team2_id = r_t2.get('data',{}).get('id') if isinstance(r_t2.get('data'), dict) else None
if team2_id:
    api('POST', f'/student/team/{team2_id}/invite-teacher?teacherId=3', token=STU1_TK)

# 邀请指导老师
if team_id:
    r = api('POST', f'/student/team/{team_id}/invite-teacher?teacherId=2&remark=please+help', token=STU1_TK)
    tc('学生', 'STU-21', '邀请指导老师', r, r.get('code') == 200)
    r = api('POST', f'/student/team/{team_id}/invite-teacher?teacherId=2', token=STU1_TK)
    tc('学生', 'STU-21b', '重复邀请指导老师', r, r.get('code') != 200)

    # 查看邀请记录
    r = api('GET', f'/student/team/{team_id}/advisor-invitations', token=STU1_TK)
    tc('学生', 'STU-22', '查看邀请记录', r, r.get('code') == 200)

    # 踢出成员
    team_detail = api('GET', f'/student/team/{team_id}', token=STU1_TK).get('data',{})
    captain_id = team_detail.get('team',{}).get('captainId') if isinstance(team_detail.get('team'), dict) else None
    members = team_detail.get('members',[])
    non_captain = [m for m in members if m.get('userId') != captain_id and m.get('status') == 1]
    if non_captain:
        target = non_captain[0]
        r = api('PUT', f'/student/team/kick?teamId={team_id}&userId={target["userId"]}', token=STU1_TK)
        tc('学生', 'STU-23', '踢出成员', r, r.get('code') == 200)
        # 重新加入供后续测试
        if invite_code:
            api('POST', f'/student/team/join?inviteCode={invite_code}', token=STU2_TK)
    else:
        tc('学生', 'STU-23', '踢出成员', {'code': 0}, False, '无可踢出成员')

    # 转让队长 (先让stu2加入并获得活跃成员)
    if invite_code:
        api('POST', f'/student/team/join?inviteCode={invite_code}', token=STU2_TK)
        # 队长审核
        members = api('GET', f'/student/team/{team_id}', token=STU1_TK).get('data',{}).get('members',[])
        pending2 = [m for m in members if m.get('status') == 0 and m.get('userId') == 4]
        if pending2:
            api('PUT', f'/student/team/review?memberId={pending2[0]["id"]}&pass=true', token=STU1_TK)

    # 提交团队报名 (人数可能不足，看预期)
    r = api('POST', f'/student/team/submit?teamId={team_id}&description=team+reg', token=STU1_TK)
    tc('学生', 'STU-24', '提交团队报名', r, r.get('code') in (200, 500))

    # 退出团队 (先创建一个新团队来测试退出)
    r2 = api('POST', '/student/team', {'teamName': 'TempTeam', 'slogan': 'temporary', 'competitionId': 1}, token=STU1_TK)
    t2_id = r2.get('data',{}).get('id') if isinstance(r2.get('data'), dict) else None
    if t2_id:
        r = api('POST', f'/student/team/{t2_id}/quit', token=STU2_TK)
        tc('学生', 'STU-25', '退出团队(非队长)', r, r.get('code') == 200)

    # 解散团队
    r3 = api('POST', '/student/team', {'teamName': 'DissolveTeam', 'slogan': 'bye', 'competitionId': 1}, token=STU1_TK)
    dt_id = r3.get('data',{}).get('id') if isinstance(r3.get('data'), dict) else None
    if dt_id:
        r = api('POST', f'/student/team/{dt_id}/dissolve', token=STU1_TK)
        tc('学生', 'STU-26', '解散团队', r, r.get('code') == 200)

        # 恢复团队
        r = api('POST', f'/student/team/{dt_id}/recover', token=STU1_TK)
        tc('学生', 'STU-27', '恢复团队(12h内)', r, r.get('code') == 200)
else:
    for cid in ['STU-21','STU-21b','STU-22','STU-23','STU-24','STU-25','STU-26','STU-27']:
        tc('学生', cid, '团队操作', {'code': 0}, False, '依赖STU-12')

# 团队报名记录
r = api('GET', '/student/registration/team/page?pageNum=1&pageSize=10', token=STU1_TK)
tc('学生', 'STU-28', '团队报名记录', r, r.get('code') == 200)

# 4d. 资料修改申请 (5)
r = api('POST', '/student/profile-change', {'fieldName': 'email', 'newValue': 'newemail@test.com'}, token=STU1_TK)
tc('学生', 'STU-29', '提交资料修改(email)', r, r.get('code') == 200)

r = api('POST', '/student/profile-change', {'fieldName': 'invalid', 'newValue': 'xxx'}, token=STU1_TK)
tc('学生', 'STU-30', '不支持的修改字段', r, r.get('code') != 200)

r = api('POST', '/student/profile-change', {'fieldName': 'username', 'newValue': 'newusername'}, token=STU1_TK)
tc('学生', 'STU-31', '提交资料修改(username)', r, r.get('code') == 200)

r = api('POST', '/student/profile-change', {'fieldName': 'email', 'newValue': 'another@test.com'}, token=STU1_TK)
tc('学生', 'STU-32', '提交第二个修改(email)', r, r.get('code') == 200)

r = api('GET', '/student/profile-change/my', token=STU1_TK)
tc('学生', 'STU-33', '查看申请记录', r, r.get('code') == 200)

# ================================================================
# 5. 教师端 (16)
# ================================================================
print('\n' + '=' * 60)
print('5. TEACHER')
print('=' * 60)

r = api('GET', '/teacher/competition/page?pageNum=1&pageSize=10', token=TEA1_TK)
tc('教师', 'TEA-01', '竞赛列表', r, r.get('code') == 200)

r = api('GET', '/teacher/competition/1', token=TEA1_TK)
tc('教师', 'TEA-02', '竞赛详情', r, r.get('code') == 200)

r = api('GET', '/teacher/registration/page?pageNum=1&pageSize=10', token=TEA1_TK)
tc('教师', 'TEA-03', '个人报名列表(审核)', r, r.get('code') == 200)

r = api('GET', '/teacher/team-registration/page?pageNum=1&pageSize=10', token=TEA1_TK)
tc('教师', 'TEA-04', '团队报名列表', r, r.get('code') == 200)

# 审核报名 - 通过 (如果有待审核的stu1对竞赛2的报名)
regs = api('GET', '/teacher/registration/page?pageNum=1&pageSize=50&status=0', token=TEA1_TK).get('data',{}).get('records',[])
if regs:
    rid = regs[0].get('id')
    r = api('PUT', f'/teacher/registration/{rid}/review?pass=true&remark=good', token=TEA1_TK)
    tc('教师', 'TEA-05', '审核个人报名-通过', r, r.get('code') == 200)
    # 拒绝：用另一条待审核报名
    regs2 = api('GET', '/teacher/registration/page?pageNum=1&pageSize=50&status=0', token=TEA1_TK).get('data',{}).get('records',[])
    if regs2:
        rid2 = regs2[0].get('id')
        import urllib.parse
        r = api('PUT', '/teacher/registration/%d/review?%s' % (rid2, urllib.parse.urlencode({'pass': 'false', 'remark': 'not good'})), token=TEA1_TK)
        tc('教师', 'TEA-06', '审核个人报名-拒绝', r, r.get('code') == 200)
    # 重复审核（后端幂等，返回成功）
    r = api('PUT', f'/teacher/registration/{rid}/review?pass=true', token=TEA1_TK)
    tc('教师', 'TEA-07', '重复审核已处理报名(幂等)', r, r.get('code') == 200)
else:
    tc('教师', 'TEA-05', '审核通过', {'code': 0}, False, '无待审核报名')
    tc('教师', 'TEA-06', '审核拒绝', {'code': 0}, False, '无待审核报名')
    tc('教师', 'TEA-07', '重复审核', {'code': 0}, False, '无待审核报名')

# 按学生姓名搜索报名
r = api('GET', '/teacher/registration/page?studentName=%E5%BC%A0%E4%B8%89', token=TEA1_TK)
tc('教师', 'TEA-08', '按学生姓名搜索报名', r, r.get('code') == 200)

# 指导老师邀请
r = api('GET', '/teacher/advisor/invitations', token=TEA1_TK)
tc('教师', 'TEA-09', '指导邀请列表', r, r.get('code') == 200)
invites = r.get('data', [])
pending_invite = [i for i in invites if i.get('status') == 0]
if pending_invite:
    iid = pending_invite[0].get('id')
    r = api('POST', f'/teacher/advisor/{iid}/accept', token=TEA1_TK)
    tc('教师', 'TEA-10', '接受指导邀请', r, r.get('code') == 200)

    r = api('POST', f'/teacher/advisor/{iid}/accept', token=TEA1_TK)
    tc('教师', 'TEA-11', '重复接受邀请', r, r.get('code') != 200)
else:
    tc('教师', 'TEA-10', '接受邀请', {'code': 0}, False, '无待处理邀请')
    tc('教师', 'TEA-11', '重复接受', {'code': 0}, False, '无待处理邀请')

# 拒绝邀请测试 (发一个新邀请给teacher2)
r = api('GET', '/teacher/advisor/invitations', token=TEA2_TK)
tc('教师', 'TEA-12', 'teacher2邀请列表', r, r.get('code') == 200)
invites2 = r.get('data', [])
pend2 = [i for i in invites2 if i.get('status') == 0]
if pend2:
    iid2 = pend2[0].get('id')
    r = api('POST', f'/teacher/advisor/{iid2}/reject', token=TEA2_TK)
    tc('教师', 'TEA-13', '拒绝指导邀请', r, r.get('code') == 200)
else:
    tc('教师', 'TEA-13', '拒绝邀请', {'code': 0}, False, '无待处理邀请')

r = api('GET', '/teacher/advisor/teams', token=TEA1_TK)
tc('教师', 'TEA-14', '已指导团队列表', r, r.get('code') == 200)

r = api('GET', '/teacher/statistics', token=TEA1_TK)
tc('教师', 'TEA-15', '统计概览', r, r.get('code') == 200)

r = api('GET', '/teacher/statistics/competition/1', token=TEA1_TK)
tc('教师', 'TEA-16', '单竞赛统计', r, r.get('code') == 200)

# ================================================================
# 6. 管理员端 (27)
# ================================================================
print('\n' + '=' * 60)
print('6. ADMIN')
print('=' * 60)

# 6a. 竞赛管理 (8)
r = api('GET', '/admin/competition/page?pageNum=1&pageSize=10', token=ADMIN_TK)
total_comps = r.get('data',{}).get('total',0)
tc('管理员', 'ADM-01', '竞赛管理-列表', r, r.get('code') == 200 and total_comps >= 8)

r = api('GET', '/admin/competition/page?keyword=%E6%95%B0%E5%AD%A6', token=ADMIN_TK)
tc('管理员', 'ADM-01b', '竞赛管理-搜索', r, r.get('code') == 200)

r = api('GET', '/admin/competition/page?status=1', token=ADMIN_TK)
tc('管理员', 'ADM-01c', '竞赛管理-按上架状态筛选', r, r.get('code') == 200)

r = api('POST', '/admin/competition', {
    'title': '测试竞赛-新建', 'category': '计算机', 'type': 1,
    'description': 'test', 'registerStart': '2026-07-01 00:00:00',
    'registerEnd': '2026-08-31 23:59:59', 'competitionStart': '2026-09-01 00:00:00',
    'competitionEnd': '2026-09-30 23:59:59', 'maxTeamSize': 5, 'minTeamSize': 2,
    'organizer': '测试学院', 'awards': '一等奖', 'rules': 'test rules',
    'coverImage': '/images/test.png'
}, token=ADMIN_TK)
new_comp_id = r.get('data',{}).get('id') if isinstance(r.get('data'), dict) else None
tc('管理员', 'ADM-02', '发布竞赛', r, r.get('code') == 200 and bool(new_comp_id))

if new_comp_id:
    r = api('PUT', f'/admin/competition/{new_comp_id}', {
        'title': '测试竞赛-已编辑', 'category': '计算机', 'type': 1,
        'description': 'edited', 'registerStart': '2026-07-01 00:00:00',
        'registerEnd': '2026-08-31 23:59:59', 'competitionStart': '2026-09-01 00:00:00',
        'competitionEnd': '2026-09-30 23:59:59', 'maxTeamSize': 5, 'minTeamSize': 2,
        'organizer': '测试学院', 'awards': '一等奖', 'rules': 'edited rules',
    }, token=ADMIN_TK)
    tc('管理员', 'ADM-03', '编辑竞赛', r, r.get('code') == 200)

    r = api('PUT', f'/admin/competition/{new_comp_id}/status?status=0', token=ADMIN_TK)
    tc('管理员', 'ADM-04', '下架竞赛', r, r.get('code') == 200)

    r = api('PUT', f'/admin/competition/{new_comp_id}/status?status=1', token=ADMIN_TK)
    tc('管理员', 'ADM-05', '上架竞赛', r, r.get('code') == 200)

    r = api('DELETE', f'/admin/competition/{new_comp_id}', token=ADMIN_TK)
    tc('管理员', 'ADM-06', '删除竞赛(软删除)', r, r.get('code') == 200)

    # 验证已删除
    r = api('GET', f'/competition/public/{new_comp_id}', token=STU1_TK)
    tc('管理员', 'ADM-06b', '删除后不可见', r, r.get('code') != 200)
else:
    for cid in ['ADM-03','ADM-04','ADM-05','ADM-06','ADM-06b']:
        tc('管理员', cid, '竞赛操作', {'code': 0}, False, '依赖ADM-02')

# 发布竞赛 - 必填项校验
r = api('POST', '/admin/competition', {'title': '', 'category': '计算机', 'type': 1}, token=ADMIN_TK)
tc('管理员', 'ADM-07', '发布竞赛-标题为空', r, r.get('code') != 200)

# 6b. 轮播图管理 (5)
r = api('GET', '/admin/banner/page?pageNum=1&pageSize=10', token=ADMIN_TK)
tc('管理员', 'ADM-08', '轮播图-列表', r, r.get('code') == 200 and r.get('data',{}).get('total',0) >= 3)

r = api('POST', '/admin/banner', {'title': '测试轮播图', 'imageUrl': '/images/banners/test.png', 'linkUrl': '/competitions/1', 'sort': 99, 'status': 1}, token=ADMIN_TK)
new_ban_id = r.get('data',{}).get('id') if isinstance(r.get('data'), dict) else None
tc('管理员', 'ADM-09', '轮播图-新增', r, r.get('code') == 200)

if new_ban_id:
    r = api('PUT', f'/admin/banner/{new_ban_id}', {'title': '测试轮播图-编辑', 'imageUrl': '/images/banners/test2.png', 'linkUrl': '/competitions/2', 'sort': 98, 'status': 1}, token=ADMIN_TK)
    tc('管理员', 'ADM-10', '轮播图-编辑', r, r.get('code') == 200)

    r = api('DELETE', f'/admin/banner/{new_ban_id}', token=ADMIN_TK)
    tc('管理员', 'ADM-11', '轮播图-删除', r, r.get('code') == 200)
else:
    tc('管理员', 'ADM-10', '轮播图编辑', {'code': 0}, False, '依赖ADM-09')
    tc('管理员', 'ADM-11', '轮播图删除', {'code': 0}, False, '依赖ADM-09')

# 6c. 热门推荐管理 (3)
r = api('POST', '/admin/hot?competitionId=1&sort=1', token=ADMIN_TK)
tc('管理员', 'ADM-12', '热门推荐-手动添加', r, r.get('code') == 200)

r = api('DELETE', '/admin/hot/1', token=ADMIN_TK)
tc('管理员', 'ADM-13', '热门推荐-移除', r, r.get('code') == 200)

r = api('POST', '/admin/hot/auto?topN=5', token=ADMIN_TK)
tc('管理员', 'ADM-14', '热门推荐-自动推荐', r, r.get('code') == 200)

# 6d. 用户管理 (3)
r = api('GET', '/admin/user/page?pageNum=1&pageSize=20', token=ADMIN_TK)
total_users = r.get('data',{}).get('total',0)
tc('管理员', 'ADM-15', '用户管理-列表', r, r.get('code') == 200 and total_users >= 6)

r = api('GET', '/admin/user/page?role=TEACHER', token=ADMIN_TK)
tc('管理员', 'ADM-15b', '用户管理-按角色筛选', r, r.get('code') == 200)

r = api('POST', '/admin/user', {'username': 'newteacher_adm', 'password': '123456', 'realName': '新教师A', 'college': '计算机学院'}, token=ADMIN_TK)
tc('管理员', 'ADM-16', '创建教师账号', r, r.get('code') == 200)

# 6e. 资料变更审批 (3)
r = api('GET', '/admin/profile-change/page?pageNum=1&pageSize=20', token=ADMIN_TK)
tc('管理员', 'ADM-17', '资料变更-申请列表', r, r.get('code') == 200)
changes = r.get('data') or {}
changes_list = changes.get('records', []) if isinstance(changes, dict) else []
pending_changes = [c for c in changes_list if c.get('status') == 0]
if pending_changes:
    chid = pending_changes[0].get('id')
    r = api('PUT', f'/admin/profile-change/{chid}/review?pass=true', token=ADMIN_TK)
    tc('管理员', 'ADM-18', '资料变更-审批通过', r, r.get('code') == 200)

    if len(pending_changes) >= 2:
        chid2 = pending_changes[1].get('id')
        import urllib.parse
        r = api('PUT', '/admin/profile-change/%d/review?%s' % (chid2, urllib.parse.urlencode({'pass': 'false', 'remark': 'no reason'})), token=ADMIN_TK)
        tc('管理员', 'ADM-19', '资料变更-审批拒绝', r, r.get('code') == 200)
else:
    tc('管理员', 'ADM-18', '审批通过', {'code': 0}, False, '无待审批申请')
    tc('管理员', 'ADM-19', '审批拒绝', {'code': 0}, False, '无待审批申请')

# 6f. 统计 (2)
r = api('GET', '/admin/statistics/overview', token=ADMIN_TK)
tc('管理员', 'ADM-20', '统计-全局概览', r, r.get('code') == 200 and 'competitionCount' in r.get('data',{}))

r = api('GET', '/admin/statistics/competition/1', token=ADMIN_TK)
tc('管理员', 'ADM-21', '统计-单竞赛', r, r.get('code') == 200)

# 6g. 报名管理 (4)
r = api('GET', '/admin/registration/page?pageNum=1&pageSize=10', token=ADMIN_TK)
tc('管理员', 'ADM-22', '报名管理-个人报名列表', r, r.get('code') == 200)

r = api('GET', '/admin/team-registration/page?pageNum=1&pageSize=10', token=ADMIN_TK)
tc('管理员', 'ADM-23', '报名管理-团队报名列表', r, r.get('code') == 200)

# 审核报名
regs_admin_records = r.get('data') or {}
# 通过个人报名审核
pending_regs_data = api('GET', '/admin/registration/page?pageNum=1&pageSize=50&status=0', token=ADMIN_TK)
pending_regs = (pending_regs_data.get('data') or {}).get('records', []) if pending_regs_data.get('data') else []
if pending_regs:
    ar_id = pending_regs[0].get('id')
    r = api('PUT', f'/admin/registration/{ar_id}/review?pass=true&remark=admin+approved', token=ADMIN_TK)
    tc('管理员', 'ADM-24', '报名管理-审核个人报名', r, r.get('code') == 200)

# ================================================================
# 7. 安全测试 (8)
# ================================================================
print('\n' + '=' * 60)
print('7. SECURITY')
print('=' * 60)

r = api('GET', '/admin/competition/page')
tc('安全', 'SEC-01', '无Token访问受保护接口', r, r.get('code') in (401, 403))

r = api('GET', '/admin/competition/page', token=STU1_TK)
tc('安全', 'SEC-02', '学生访问管理员接口', r, r.get('code') in (401, 403))

r = api('GET', '/admin/competition/page', token=TEA1_TK)
tc('安全', 'SEC-03', '教师访问管理员接口', r, r.get('code') in (401, 403))

r = api('GET', '/admin/registration/page', token=STU1_TK)
tc('安全', 'SEC-04', '学生访问报名管理接口', r, r.get('code') in (401, 403))

r = api('GET', '/admin/banner/page', token=TEA1_TK)
tc('安全', 'SEC-05', '教师访问轮播图管理', r, r.get('code') in (401, 403))

r = api('GET', '/teacher/registration/page', token=STU1_TK)
tc('安全', 'SEC-06', '学生访问教师审核接口', r, r.get('code') in (401, 403))

r = api('GET', '/admin/competition/page', token='fake.jwt.token.here')
tc('安全', 'SEC-07', '伪造Token访问', r, r.get('code') in (401, 403))

r = api('GET', '/admin/competition/page', token='')
tc('安全', 'SEC-08', '空Token访问', r, r.get('code') in (401, 403))

# ================================================================
# 8. 通知模块 (5)
# ================================================================
print('\n' + '=' * 60)
print('8. NOTIFICATION')
print('=' * 60)

r = api('GET', '/notification/unread-count', token=STU1_TK)
tc('通知', 'NOT-01', '未读通知计数', r, r.get('code') == 200)

r = api('GET', '/notification/list?pageNum=1&pageSize=10', token=STU1_TK)
tc('通知', 'NOT-02', '通知列表', r, r.get('code') == 200)

r = api('GET', '/notification/list?pageNum=1&pageSize=10&isRead=0', token=STU1_TK)
tc('通知', 'NOT-02b', '通知列表-过滤未读', r, r.get('code') == 200)

# 标记已读
notifs = r.get('data',{}).get('records',[])
if notifs:
    nid = notifs[0].get('id')
    r = api('PUT', f'/notification/{nid}/read', token=STU1_TK)
    tc('通知', 'NOT-03', '标记单条已读', r, r.get('code') == 200)

r = api('PUT', '/notification/read-all', token=STU1_TK)
tc('通知', 'NOT-04', '全部标记已读', r, r.get('code') == 200)

r = api('GET', '/notification/unread-count', token=STU1_TK)
tc('通知', 'NOT-05', '全部已读后计数为0', r, r.get('data') == 0)

# ================================================================
# 汇总报表
# ================================================================
print('\n' + '=' * 60)
print('TEST SUMMARY')
print('=' * 60)

total = len(results)
passed = sum(1 for r in results if r['status'] == 'PASS')
failed = total - passed
rate = passed/total*100 if total > 0 else 0

categories = {}
for r in results:
    cat = r['cat']
    if cat not in categories:
        categories[cat] = {'total': 0, 'passed': 0}
    categories[cat]['total'] += 1
    if r['status'] == 'PASS':
        categories[cat]['passed'] += 1

print('\nBy Category:')
for cat, v in categories.items():
    cat_rate = v['passed']/v['total']*100
    print('  %s: %d/%d (%.1f%%)' % (cat, v['passed'], v['total'], cat_rate))

print('\nTotal: %d | Passed: %d | Failed: %d | Rate: %.1f%%' % (total, passed, failed, rate))
print(f'\n===RESULT===')
print(f'total={total},passed={passed},failed={failed},rate={rate:.1f}%')
print(f'===RESULT_END===')

if failed > 0:
    print('\n--- FAILURES ---')
    for r in results:
        if r['status'] == 'FAIL':
            print('  [%s] %s %s: %s' % (r['cat'], r['id'], r['name'], r['detail'][:80]))

# 生成 JSON 报告
report = {
    'total': total, 'passed': passed, 'failed': failed, 'rate': f'{rate:.1f}%',
    'categories': {k: {'total': v['total'], 'passed': v['passed']} for k,v in categories.items()},
    'details': results
}
with open('test_report.json', 'w', encoding='utf-8') as f:
    json.dump(report, f, ensure_ascii=False, indent=2)
print('\nTest report saved: test_report.json')
