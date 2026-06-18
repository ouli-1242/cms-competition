<script setup lang="ts">
/**
 * 报名管理页（管理员 / 老师）
 * - 5 列表格：报名人 / 竞赛名称 / 参赛类型 / 审核状态 / 操作
 * - 筛选：竞赛下拉 + 状态下拉 + 姓名搜索
 * - 4 个弹窗：详情 / 审核 / 导出成功 / 列表
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRegistrations, auditRegistration, getTeamRegistrations, auditTeamRegistration, getCompetitions } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const list = ref<any[]>([])

// 筛选
const filter = reactive({
  competitionId: '',
  status: '',
  keyword: ''
})

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref<any>(null)

// 审核弹窗
const auditVisible = ref(false)
const auditTarget = ref<any>(null)
const auditForm = reactive({ result: 'APPROVE', remark: '' })
const auditErrors = reactive({ remark: '' })
const submitting = ref(false)

// Tab 切换
const activeTab = ref<'individual' | 'team'>('individual')

// 竞赛选项（动态加载）
const competitionOptions = ref<any[]>([])
const compTitleMap = ref<Record<number, string>>({})

// 团队报名
const teamList = ref<any[]>([])
const teamLoading = ref(false)
const teamTotal = ref(0)
const teamPage = ref(1)

// 团队审核弹窗
const teamAuditVisible = ref(false)
const teamAuditTarget = ref<any>(null)
const teamAuditForm = reactive({ result: 'APPROVE', remark: '' })
const teamAuditErrors = reactive({ remark: '' })
const teamSubmitting = ref(false)

// 团队报名详情
const teamDetailVisible = ref(false)
const teamDetailData = ref<any>(null)

// 导出成功弹窗
const exportVisible = ref(false)

// ====== 占位数据 ======
const placeholder = [
  { id: 1, realName: '张三', competitionTitle: '程序设计竞赛', type: 'TEAM', status: 0, statusText: '待审核' },
  { id: 2, realName: '李四', competitionTitle: '英语演讲比赛', type: 'INDIVIDUAL', status: 1, statusText: '已通过' },
  { id: 3, realName: '王五', competitionTitle: '软件设计竞赛', type: 'INDIVIDUAL', status: 2, statusText: '已拒绝' }
]

async function loadData() {
  loading.value = true
  try {
    const res: any = await getRegistrations({
      pageNum: 1,
      pageSize: 100,
      competitionId: filter.competitionId ? Number(filter.competitionId) : undefined,
      status: filter.status !== '' ? Number(filter.status) : undefined,
      studentName: filter.keyword || undefined
    })
    list.value = res?.records || []
  } catch (e) {
    list.value = placeholder
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (activeTab.value === 'individual') {
    loadData()
  } else {
    teamPage.value = 1
    loadTeamData()
  }
}

function handleReset() {
  filter.competitionId = ''
  filter.status = ''
  filter.keyword = ''
  if (activeTab.value === 'individual') {
    loadData()
  } else {
    teamPage.value = 1
    loadTeamData()
  }
}

function openDetail(row: any) {
  detailData.value = row
  detailVisible.value = true
}

function openAudit(row: any) {
  auditTarget.value = row
  auditForm.result = 'APPROVE'
  auditForm.remark = ''
  auditErrors.remark = ''
  auditVisible.value = true
}

function statusType(s: number) {
  return s === 0 ? 'warning' : s === 1 ? 'success' : 'danger'
}

function typeText(t: string) {
  return t === 'TEAM' ? '团队' : '个人'
}

function validateAudit(): boolean {
  auditErrors.remark = ''
  if (!auditForm.remark.trim()) {
    auditErrors.remark = '请填写审核意见'
    return false
  }
  if (auditForm.remark.length > 200) {
    auditErrors.remark = '审核意见不能超过 200 字'
    return false
  }
  return true
}

async function handleAuditSubmit() {
  if (!validateAudit()) return
  if (!auditTarget.value) return
  submitting.value = true
  try {
    await auditRegistration(
      auditTarget.value.id,
      auditForm.result === 'APPROVE',
      auditForm.remark
    )
    // 本地更新
    const target = list.value.find((x) => x.id === auditTarget.value.id)
    if (target) {
      target.status = auditForm.result === 'APPROVE' ? 1 : 2
      target.statusText = auditForm.result === 'APPROVE' ? '已通过' : '已拒绝'
    }
    ElMessage.success('审核完成')
    auditVisible.value = false
  } finally {
    submitting.value = false
  }
}

// ====== 竞赛选项加载 ======
async function loadCompetitions(type?: number) {
  try {
    const res: any = await getCompetitions({ pageNum: 1, pageSize: 200, type })
    const records = res?.records || []
    competitionOptions.value = records
    const map: Record<number, string> = {}
    records.forEach((c: any) => { map[c.id] = c.title })
    compTitleMap.value = map
  } catch {
    competitionOptions.value = []
  }
}

// ====== 团队报名 ======
async function loadTeamData() {
  teamLoading.value = true
  try {
    const res: any = await getTeamRegistrations({
      pageNum: teamPage.value,
      pageSize: 100,
      competitionId: filter.competitionId ? Number(filter.competitionId) : undefined,
      status: filter.status !== '' ? Number(filter.status) : undefined
    })
    teamList.value = res?.records || []
    teamTotal.value = res?.total || 0
  } catch {
    teamList.value = []
    teamTotal.value = 0
  } finally {
    teamLoading.value = false
  }
}

function handleTabChange(tab: 'individual' | 'team') {
  activeTab.value = tab
  filter.competitionId = ''
  const type = tab === 'individual' ? 1 : 2
  loadCompetitions(type)
  if (tab === 'individual') {
    loadData()
  } else {
    teamPage.value = 1
    loadTeamData()
  }
}

function openTeamAudit(row: any) {
  teamAuditTarget.value = row
  teamAuditForm.result = 'APPROVE'
  teamAuditForm.remark = ''
  teamAuditErrors.remark = ''
  teamAuditVisible.value = true
}

function openTeamDetail(row: any) {
  teamDetailData.value = row
  teamDetailVisible.value = true
}

function validateTeamAudit(): boolean {
  teamAuditErrors.remark = ''
  if (!teamAuditForm.remark.trim()) {
    teamAuditErrors.remark = '请填写审核意见'
    return false
  }
  if (teamAuditForm.remark.length > 200) {
    teamAuditErrors.remark = '审核意见不能超过 200 字'
    return false
  }
  return true
}

async function handleTeamAuditSubmit() {
  if (!validateTeamAudit()) return
  if (!teamAuditTarget.value) return
  teamSubmitting.value = true
  try {
    await auditTeamRegistration(
      teamAuditTarget.value.id,
      teamAuditForm.result === 'APPROVE',
      teamAuditForm.remark
    )
    const target = teamList.value.find((x) => x.id === teamAuditTarget.value.id)
    if (target) {
      target.status = teamAuditForm.result === 'APPROVE' ? 1 : 2
      target.statusText = teamAuditForm.result === 'APPROVE' ? '已通过' : '已拒绝'
    }
    ElMessage.success('审核完成')
    teamAuditVisible.value = false
  } finally {
    teamSubmitting.value = false
  }
}

function handleExport() {
  // 实际项目中调用 API 导出
  // await exportRegistrations(filter)
  exportVisible.value = true
}

onMounted(() => {
  loadCompetitions(1)
  loadData()
})
</script>

<template>
  <div class="page">
    <!-- 顶部栏 -->
    <div class="page-header">
      <h2 class="page-title">报名管理</h2>
      <button class="btn-export" @click="handleExport">
        <span>📥</span>报名导出
      </button>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <button
        class="tab-item"
        :class="{ active: activeTab === 'individual' }"
        @click="handleTabChange('individual')"
      >个人报名</button>
      <button
        class="tab-item"
        :class="{ active: activeTab === 'team' }"
        @click="handleTabChange('team')"
      >团队报名</button>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <div class="filter-item">
        <select v-model="filter.competitionId" class="form-input form-select" @change="handleSearch">
          <option value="">全部竞赛</option>
          <option v-for="c in competitionOptions" :key="c.id" :value="String(c.id)">{{ c.title }}</option>
        </select>
      </div>
      <div class="filter-item">
        <select v-model="filter.status" class="form-input form-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已通过</option>
          <option value="2">已拒绝</option>
        </select>
      </div>
      <div class="filter-item filter-search">
        <input
          v-model="filter.keyword"
          type="text"
          class="form-input"
          placeholder="搜索姓名"
          @keyup.enter="handleSearch"
        />
      </div>
      <div class="filter-actions">
        <button class="btn-search" @click="handleSearch">搜索</button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- 个人报名 表格 -->
    <el-table
      v-show="activeTab === 'individual'"
      v-loading="loading"
      :data="list"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
    >
      <el-table-column prop="studentName" label="报名人" width="120" align="center">
        <template #default="{ row }">
          <span class="user-name">{{ row.studentName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="competitionName" label="竞赛名称" min-width="200">
        <template #default="{ row }">
          <span class="comp-name">{{ row.competitionName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="college" label="学院" width="140" align="center">
        <template #default="{ row }">
          {{ row.college || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
        <template #default="{ row }">
          {{ row.registerTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>
            {{ row.status === 0 ? '待审核' : row.status === 1 ? '已通过' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="info" size="small" plain round @click="openDetail(row)">查看详情</el-button>
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            plain
            round
            @click="openAudit(row)"
          >
            审核
          </el-button>
          <el-button v-else type="info" size="small" disabled round>已审核</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无报名" />
      </template>
    </el-table>

    <!-- 团队报名 表格 -->
    <el-table
      v-show="activeTab === 'team'"
      v-loading="teamLoading"
      :data="teamList"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
    >
      <el-table-column prop="teamName" label="团队名称" width="140" align="center">
        <template #default="{ row }">
          <span class="user-name">{{ row.teamName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="competitionName" label="竞赛名称" min-width="200">
        <template #default="{ row }">
          <span class="comp-name">{{ row.competitionName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="captainName" label="队长" width="100" align="center">
        <template #default="{ row }">
          {{ row.captainName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="成员" width="90" align="center">
        <template #default="{ row }">
          {{ row.memberCount || 0 }}{{ row.maxSize ? '/' + row.maxSize : '' }}
        </template>
      </el-table-column>
      <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
        <template #default="{ row }">
          {{ row.registerTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>
            {{ row.status === 0 ? '待审核' : row.status === 1 ? '已通过' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="info" size="small" plain round @click="openTeamDetail(row)">查看详情</el-button>
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            plain
            round
            @click="openTeamAudit(row)"
          >
            审核
          </el-button>
          <el-button v-else type="info" size="small" disabled round>已审核</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无团队报名" />
      </template>
    </el-table>

    <!-- 报名详情 弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="报名详情"
      width="520px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="detail-body" v-if="detailData">
        <div class="detail-row">
          <span class="detail-label">报名人：</span>
          <span class="detail-value">{{ detailData.studentName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">学院：</span>
          <span class="detail-value">{{ detailData.college || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">竞赛：</span>
          <span class="detail-value">{{ detailData.competitionName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报名时间：</span>
          <span class="detail-value">{{ detailData.registerTime || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态：</span>
          <span class="detail-value">
            <el-tag :type="statusType(detailData.status) as any" size="small" effect="light" round>
              {{ detailData.status === 0 ? '待审核' : detailData.status === 1 ? '已通过' : '已拒绝' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-row" v-if="detailData.description">
          <span class="detail-label">说明：</span>
          <span class="detail-value">{{ detailData.description }}</span>
        </div>
        <div class="detail-row" v-if="detailData.reviewRemark">
          <span class="detail-label">审核意见：</span>
          <span class="detail-value">{{ detailData.reviewRemark }}</span>
        </div>
        <div class="detail-row" v-if="detailData.attachment">
          <span class="detail-label">附件：</span>
          <span class="detail-value">
            <a :href="detailData.attachment" target="_blank" class="link-text">查看附件</a>
          </span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok gray" @click="detailVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>

    <!-- 团队报名详情 弹窗 -->
    <el-dialog
      v-model="teamDetailVisible"
      title="团队报名详情"
      width="520px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="detail-body" v-if="teamDetailData">
        <div class="detail-row">
          <span class="detail-label">团队名称：</span>
          <span class="detail-value">{{ teamDetailData.teamName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">竞赛：</span>
          <span class="detail-value">{{ teamDetailData.competitionName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">队长：</span>
          <span class="detail-value">{{ teamDetailData.captainName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">成员数：</span>
          <span class="detail-value">{{ teamDetailData.memberCount || 0 }}{{ teamDetailData.maxSize ? ' / ' + teamDetailData.maxSize : '' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报名时间：</span>
          <span class="detail-value">{{ teamDetailData.registerTime || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态：</span>
          <span class="detail-value">
            <el-tag :type="statusType(teamDetailData.status) as any" size="small" effect="light" round>
              {{ teamDetailData.status === 0 ? '待审核' : teamDetailData.status === 1 ? '已通过' : '已拒绝' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-row" v-if="teamDetailData.description">
          <span class="detail-label">说明：</span>
          <span class="detail-value">{{ teamDetailData.description }}</span>
        </div>
        <div class="detail-row" v-if="teamDetailData.reviewRemark">
          <span class="detail-label">审核意见：</span>
          <span class="detail-value">{{ teamDetailData.reviewRemark }}</span>
        </div>
        <div class="detail-row" v-if="teamDetailData.attachment">
          <span class="detail-label">附件：</span>
          <span class="detail-value">
            <a :href="teamDetailData.attachment" target="_blank" class="link-text">查看附件</a>
          </span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok gray" @click="teamDetailVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>

    <!-- 报名审核 弹窗 -->
    <el-dialog
      v-model="auditVisible"
      title="报名审核"
      width="480px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="audit-body">
        <div class="form-item">
          <label class="form-label">审核结果</label>
          <select v-model="auditForm.result" class="form-input form-select">
            <option value="APPROVE">通过</option>
            <option value="REJECT">拒绝</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">审核意见</label>
          <textarea
            v-model="auditForm.remark"
            class="form-input form-textarea"
            :class="{ 'is-error': auditErrors.remark }"
            placeholder="请填写审核意见"
            maxlength="200"
            rows="4"
          ></textarea>
          <p v-if="auditErrors.remark" class="error-text">{{ auditErrors.remark }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-ok gray" @click="auditVisible = false">取消</button>
          <button class="btn-ok primary" :disabled="submitting" @click="handleAuditSubmit">
            {{ submitting ? '提交中...' : '确认提交' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 团队报名审核 弹窗 -->
    <el-dialog
      v-model="teamAuditVisible"
      title="团队报名审核"
      width="480px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="audit-body">
        <div class="form-item">
          <label class="form-label">审核结果</label>
          <select v-model="teamAuditForm.result" class="form-input form-select">
            <option value="APPROVE">通过</option>
            <option value="REJECT">拒绝</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">审核意见</label>
          <textarea
            v-model="teamAuditForm.remark"
            class="form-input form-textarea"
            :class="{ 'is-error': teamAuditErrors.remark }"
            placeholder="请填写审核意见"
            maxlength="200"
            rows="4"
          ></textarea>
          <p v-if="teamAuditErrors.remark" class="error-text">{{ teamAuditErrors.remark }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-ok gray" @click="teamAuditVisible = false">取消</button>
          <button class="btn-ok primary" :disabled="teamSubmitting" @click="handleTeamAuditSubmit">
            {{ teamSubmitting ? '提交中...' : '确认提交' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 导出成功 弹窗 -->
    <el-dialog
      v-model="exportVisible"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header"><h3>导出成功</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">报名数据导出成功!</p>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok primary" @click="exportVisible = false">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.btn-export {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 $space-5;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
  transition: all $transition-fast;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
  }
}

// ===== Tab 切换 =====
.tab-bar {
  display: flex;
  gap: 0;
  border-bottom: 2px solid $border-light;
}

.tab-item {
  padding: $space-2 $space-5;
  border: none;
  background: transparent;
  color: $text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  cursor: pointer;
  position: relative;
  transition: all $transition-fast;

  &::after {
    content: '';
    position: absolute;
    left: 0;
    bottom: -2px;
    width: 100%;
    height: 2px;
    background: transparent;
    transition: background $transition-fast;
  }

  &:hover {
    color: $primary;
  }

  &.active {
    color: $primary;
    font-weight: $font-weight-semibold;

    &::after {
      background: $primary;
    }
  }
}

// ===== 筛选 =====
.filter-bar {
  display: flex;
  gap: $space-3;
  align-items: center;
  padding: $space-3 $space-4;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
}

.filter-item {
  flex: 0 0 200px;
  &.filter-search {
    flex: 1 1 auto;
  }
}

.filter-actions {
  display: flex;
  gap: $space-2;
  flex-shrink: 0;
}

.form-input {
  height: 36px;
  padding: 0 $space-3;
  border: 1.5px solid $border-base;
  background: $bg-card;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  color: $text-primary;
  outline: none;
  transition: all $transition-fast;
  font-family: inherit;
  width: 100%;

  &:focus {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }
  &.is-error {
    border-color: $danger;
    background: #fef5f5;
  }
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12'%3E%3Cpath fill='%23a0aec0' d='M6 8.5L1.5 4h9z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 12px;
  padding-right: 32px;
  cursor: pointer;
}

.form-textarea {
  height: auto;
  min-height: 100px;
  padding: $space-2 $space-3;
  resize: vertical;
  line-height: 1.6;
  font-family: inherit;
}

.btn-search {
  height: 36px;
  padding: 0 $space-5;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    transform: translateY(-1px);
  }
}

.btn-reset {
  height: 36px;
  padding: 0 $space-5;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }
}

// ===== 表格 =====
.data-table {
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
  background: $bg-card;
}

.user-name,
.comp-name {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

// ===== 详情 =====
.detail-body {
  padding: $space-3 0;
}

.detail-row {
  display: flex;
  align-items: center;
  padding: $space-2 0;
  font-size: $font-size-sm;
  border-bottom: 1px dashed $border-light;

  &:last-child {
    border-bottom: none;
  }
}

.detail-label {
  width: 80px;
  color: $text-secondary;
  flex-shrink: 0;
}

.detail-value {
  color: $text-primary;
  flex: 1;
}

.link-text {
  color: $primary;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
}

// ===== 审核 =====
.audit-body {
  display: flex;
  flex-direction: column;
  gap: $space-4;
  padding: $space-3 0;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: $space-1;
}

.form-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-regular;
}

.error-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $danger;
}

// ===== 弹窗通用 =====
:deep(.el-dialog__header) {
  padding: $space-5 $space-6 0;
  margin-right: 0;
}

.dialog-header {
  h3 {
    margin: 0;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }
}

.dialog-body {
  padding: $space-3 0;
}

.dialog-text {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.7;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  padding-top: $space-3;

  &.center {
    justify-content: center;
  }
}

.btn-ok {
  height: 36px;
  padding: 0 $space-5;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  min-width: 80px;
  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }

  &.primary {
    background: linear-gradient(135deg, $primary, $primary-hover);
    color: #fff;
    border: none;
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
      color: #fff;
    }
    &:disabled {
      background: $bg-disabled;
      color: $text-disabled;
      cursor: not-allowed;
      box-shadow: none;
    }
  }
  &.gray {
    background: #e2e8f0;
    border-color: #cbd5e0;
    color: $text-regular;
    &:hover {
      background: #cbd5e0;
      border-color: #a0aec0;
      color: $text-primary;
    }
  }
}

@media (max-width: 768px) {
  .filter-bar {
    flex-wrap: wrap;
  }
  .filter-item {
    flex: 1 1 calc(50% - $space-2);
  }
}
</style>
