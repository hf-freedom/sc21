import { createRouter, createWebHistory } from 'vue-router'
import UserList from '../views/UserList.vue'
import RiskRules from '../views/RiskRules.vue'
import Transactions from '../views/Transactions.vue'
import InitiateTransaction from '../views/InitiateTransaction.vue'
import PendingReview from '../views/PendingReview.vue'
import Blacklist from '../views/Blacklist.vue'

const routes = [
  {
    path: '/',
    redirect: '/users'
  },
  {
    path: '/users',
    name: 'UserList',
    component: UserList
  },
  {
    path: '/rules',
    name: 'RiskRules',
    component: RiskRules
  },
  {
    path: '/transactions',
    name: 'Transactions',
    component: Transactions
  },
  {
    path: '/initiate',
    name: 'InitiateTransaction',
    component: InitiateTransaction
  },
  {
    path: '/pending',
    name: 'PendingReview',
    component: PendingReview
  },
  {
    path: '/blacklist',
    name: 'Blacklist',
    component: Blacklist
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
