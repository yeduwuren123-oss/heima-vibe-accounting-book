import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/add',
    children: [
      { path: 'add', name: 'AddRecord', component: () => import('../views/AddRecord.vue'), meta: { title: '记账' } },
      { path: 'list', name: 'RecordList', component: () => import('../views/RecordList.vue'), meta: { title: '记录' } },
      { path: 'stats', name: 'Stats', component: () => import('../views/Stats.vue'), meta: { title: '统计' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
