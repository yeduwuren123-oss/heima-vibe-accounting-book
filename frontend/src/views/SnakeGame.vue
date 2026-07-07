<template>
  <div class="snake-game-page">
    <!-- 顶部设置栏 -->
    <el-card style="margin-bottom: 16px">
      <div class="settings-bar">
        <div class="setting-group">
          <span class="label">模式：</span>
          <el-radio-group v-model="gameMode" @change="resetGame" size="small">
            <el-radio-button value="classic">经典</el-radio-button>
            <el-radio-button value="wrap">穿墙</el-radio-button>
            <el-radio-button value="maze">迷宫</el-radio-button>
            <el-radio-button value="timed">限时(60s)</el-radio-button>
          </el-radio-group>
        </div>
        <div class="setting-group">
          <span class="label">难度：</span>
          <el-radio-group v-model="difficulty" @change="resetGame" size="small">
            <el-radio-button value="easy">简单</el-radio-button>
            <el-radio-button value="medium">中等</el-radio-button>
            <el-radio-button value="hard">困难</el-radio-button>
          </el-radio-group>
        </div>
        <div class="score-display">
          <span>得分：<strong>{{ score }}</strong></span>
          <span style="margin-left: 16px">最高：<strong>{{ highScore }}</strong></span>
          <span v-if="gameMode === 'timed'" style="margin-left: 16px; color: #e6a23c">
            剩余：<strong>{{ remainingTime }}s</strong>
          </span>
        </div>
        <el-button type="primary" @click="handleMainButton" :disabled="gameState === 'playing'">
          {{ gameState === 'idle' ? '开始游戏' : gameState === 'paused' ? '继续' : '重新开始' }}
        </el-button>
      </div>
    </el-card>

    <!-- 游戏画布 -->
    <el-card style="display: flex; justify-content: center">
      <div class="canvas-wrapper">
        <canvas
          ref="canvasRef"
          :width="CANVAS_SIZE"
          :height="CANVAS_SIZE"
          tabindex="0"
          @keydown="handleKeydown"
        />
        <!-- 暂停遮罩 -->
        <div v-if="gameState === 'paused'" class="pause-overlay">
          <span>已暂停 — 按 Enter 继续</span>
        </div>
        <!-- 游戏结束遮罩 -->
        <div v-if="gameState === 'gameover'" class="gameover-overlay">
          <div>游戏结束</div>
          <div style="font-size: 24px; margin-top: 8px">得分：{{ score }}</div>
          <div style="font-size: 14px; margin-top: 4px; color: #ccc">最高分：{{ highScore }}</div>
          <el-button type="primary" size="small" style="margin-top: 12px" @click="startGame">再来一局</el-button>
        </div>
        <!-- 初始提示 -->
        <div v-if="gameState === 'idle'" class="pause-overlay">
          <span>选择模式和难度后，点击"开始游戏"</span>
        </div>
      </div>
    </el-card>

    <!-- 操作说明 -->
    <el-card style="margin-top: 16px">
      <div style="display: flex; gap: 24px; justify-content: center; font-size: 13px; color: #666">
        <span>↑↓←→ 移动</span>
        <span>按住空格键 加速</span>
        <span>Enter 暂停/继续</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'

// ==================== 常量 ====================
const CELL_SIZE = 30
const COLS = 20
const ROWS = 20
const CANVAS_SIZE = CELL_SIZE * COLS // 600

const SPEED_MAP = { easy: 150, medium: 100, hard: 60 }

// 迷宫障碍物（固定的墙体布局）
const MAZE_WALLS = (() => {
  const walls = new Set()
  // 几道横墙和竖墙，形成走廊
  const patterns = [
    // 横墙
    { x1: 2, y1: 5, x2: 12, y2: 5 },
    { x1: 7, y1: 10, x2: 17, y2: 10 },
    { x1: 2, y1: 14, x2: 10, y2: 14 },
    // 竖墙
    { x1: 5, y1: 2, x2: 5, y2: 8 },
    { x1: 14, y1: 8, x2: 14, y2: 18 },
    { x1: 10, y1: 11, x2: 10, y2: 16 },
    // 方块
    { x1: 15, y1: 2, x2: 16, y2: 3 },
    { x1: 3, y1: 16, x2: 5, y2: 17 },
  ]
  patterns.forEach(({ x1, y1, x2, y2 }) => {
    for (let x = x1; x <= x2; x++) {
      for (let y = y1; y <= y2; y++) {
        walls.add(`${x},${y}`)
      }
    }
  })
  return walls
})()

// ==================== Vue 状态 ====================
const canvasRef = ref(null)
const gameMode = ref('classic')
const difficulty = ref('medium')
const gameState = ref('idle') // idle | playing | paused | gameover
const score = ref(0)
const highScore = ref(0)
const remainingTime = ref(60)

// ==================== 游戏内部变量（非响应式，避免性能开销） ====================
let canvas, ctx
let snake = []
let direction = { x: 1, y: 0 }
let nextDirection = { x: 1, y: 0 }
let food = null
let redFood = null
let redFoodTimer = 0
let bombs = []
let bombTimer = 0
let animationId = null
let lastStepTime = 0
let speedBoost = false
let countdownInterval = null

// ==================== 辅助函数 ====================
function key(x, y) { return `${x},${y}` }

function occupiedSet() {
  const set = new Set()
  snake.forEach(s => set.add(key(s.x, s.y)))
  if (food) set.add(key(food.x, food.y))
  if (redFood) set.add(key(redFood.x, redFood.y))
  bombs.forEach(b => set.add(key(b.x, b.y)))
  if (gameMode.value === 'maze') {
    MAZE_WALLS.forEach(k => set.add(k))
  }
  return set
}

function randomCell() {
  const occupied = occupiedSet()
  const free = []
  for (let x = 0; x < COLS; x++) {
    for (let y = 0; y < ROWS; y++) {
      if (!occupied.has(key(x, y))) {
        free.push({ x, y })
      }
    }
  }
  if (free.length === 0) return null
  return free[Math.floor(Math.random() * free.length)]
}

function spawnFood() {
  const cell = randomCell()
  if (cell) {
    food = cell
  }
}

function spawnRedFood() {
  const cell = randomCell()
  if (cell) {
    redFood = cell
    redFoodTimer = 5000 // 5 秒后消失
  }
}

function spawnBombs() {
  bombs = []
  for (let i = 0; i < 4; i++) {
    const cell = randomCell()
    if (cell) {
      bombs.push(cell)
    }
  }
  bombTimer = 8000 // 8 秒后重新生成位置
}

// ==================== 游戏逻辑 ====================
function initGame() {
  // 蛇初始位置：中间偏左，长度 4，方向向右
  const startX = Math.floor(COLS / 2)
  const startY = Math.floor(ROWS / 2)
  snake = [
    { x: startX, y: startY },
    { x: startX - 1, y: startY },
    { x: startX - 2, y: startY },
    { x: startX - 3, y: startY },
  ]
  direction = { x: 1, y: 0 }
  nextDirection = { x: 1, y: 0 }
  food = null
  redFood = null
  redFoodTimer = 0
  bombs = []
  bombTimer = 0
  speedBoost = false
  lastStepTime = 0

  spawnFood()
  if (gameMode.value === 'maze') {
    spawnBombs()
  }
  if (gameMode.value === 'timed') {
    remainingTime.value = 60
    startCountdown()
  }
}

function startCountdown() {
  clearInterval(countdownInterval)
  countdownInterval = setInterval(() => {
    if (gameState.value !== 'playing' || document.hidden) return
    remainingTime.value--
    if (remainingTime.value <= 0) {
      endGame()
    }
  }, 1000)
}

function handleMainButton() {
  if (gameState.value === 'paused') {
    resumeGame()
  } else {
    startGame()
  }
}

function startGame() {
  if (gameState.value === 'playing') return // 防止双击重复启动
  initGame()
  gameState.value = 'playing'
  score.value = 0
  lastStepTime = 0
  canvasRef.value?.focus()
  if (animationId) cancelAnimationFrame(animationId)
  gameLoop(0)
}

function resumeGame() {
  gameState.value = 'playing'
  lastStepTime = 0
  canvasRef.value?.focus()
  if (animationId) cancelAnimationFrame(animationId)
  gameLoop(0)
}

function endGame() {
  gameState.value = 'gameover'
  clearInterval(countdownInterval)
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  // 更新最高分
  if (score.value > highScore.value) {
    highScore.value = score.value
    try { localStorage.setItem('snake-high-score', highScore.value) } catch {}
  }
  draw() // 重新绘制，显示遮罩
}

function resetGame() {
  clearInterval(countdownInterval)
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  gameState.value = 'idle'
  score.value = 0
  if (gameMode.value === 'timed') remainingTime.value = 60
  // 清除所有游戏对象，避免 idle 画面残留
  food = null
  redFood = null
  redFoodTimer = 0
  bombs = []
  bombTimer = 0
  speedBoost = false
  // 重置蛇，仅用于 idle 状态的绘制
  const startX = Math.floor(COLS / 2)
  const startY = Math.floor(ROWS / 2)
  snake = [
    { x: startX, y: startY },
    { x: startX - 1, y: startY },
    { x: startX - 2, y: startY },
    { x: startX - 3, y: startY },
  ]
  direction = { x: 1, y: 0 }
  nextDirection = { x: 1, y: 0 }
  draw()
}

function moveSnake() {
  direction = { ...nextDirection }
  const head = snake[0]
  let nx = head.x + direction.x
  let ny = head.y + direction.y

  // 穿墙模式
  if (gameMode.value === 'wrap') {
    if (nx < 0) nx = COLS - 1
    if (nx >= COLS) nx = 0
    if (ny < 0) ny = ROWS - 1
    if (ny >= ROWS) ny = 0
  }

  // 撞墙检测（非穿墙模式）
  if (gameMode.value !== 'wrap') {
    if (nx < 0 || nx >= COLS || ny < 0 || ny >= ROWS) {
      endGame()
      return
    }
  }

  // 迷宫障碍物检测
  if (gameMode.value === 'maze' && MAZE_WALLS.has(key(nx, ny))) {
    endGame()
    return
  }

  // 炸弹检测（迷宫模式）
  for (const bomb of bombs) {
    if (bomb.x === nx && bomb.y === ny) {
      endGame()
      return
    }
  }

  // 撞自己检测（检查新头部是否与除尾部外的蛇身重合）
  for (let i = 0; i < snake.length - 1; i++) {
    if (snake[i].x === nx && snake[i].y === ny) {
      endGame()
      return
    }
  }

  // 移动蛇
  const newHead = { x: nx, y: ny }
  snake.unshift(newHead)

  // 吃食物判定
  let ate = false
  if (food && food.x === nx && food.y === ny) {
    score.value += 10
    food = null
    spawnFood()
    ate = true
  }
  if (redFood && redFood.x === nx && redFood.y === ny) {
    score.value += 30
    redFood = null
    redFoodTimer = 0
    ate = true
  }

  if (!ate) {
    snake.pop() // 没吃到食物才移除尾部
  }
}

function gameLoop(timestamp) {
  // 非 playing 状态：只绘制一次即停止，不再循环
  if (gameState.value !== 'playing') {
    draw()
    return
  }

  if (lastStepTime === 0) {
    lastStepTime = timestamp
  }

  const baseInterval = SPEED_MAP[difficulty.value]
  const interval = speedBoost ? baseInterval / 2 : baseInterval
  const elapsed = timestamp - lastStepTime

  // 红色食物计时
  if (redFood && redFoodTimer > 0) {
    redFoodTimer -= elapsed
    if (redFoodTimer <= 0) {
      redFood = null
      redFoodTimer = 0
      if (Math.random() < 0.3) {
        spawnRedFood()
      }
    }
  } else if (!redFood && Math.random() < 0.003) {
    spawnRedFood()
  }

  // 按节拍移动（先移动蛇，再生成炸弹，避免炸弹出现在蛇前进方向）
  if (elapsed >= interval) {
    moveSnake()
    // moveSnake 内部可能触发 endGame()，若已结束则停止循环
    if (gameState.value !== 'playing') {
      draw()
      return
    }
    lastStepTime = timestamp
  }

  // 炸弹重新生成（迷宫模式）—— 放在蛇移动之后，确保不会生成在蛇新位置
  if (gameMode.value === 'maze' && bombTimer > 0) {
    bombTimer -= elapsed
    if (bombTimer <= 0) {
      spawnBombs()
    }
  }

  draw()
  animationId = requestAnimationFrame(gameLoop)
}

// ==================== 绘制 ====================
function draw() {
  if (!ctx) return

  // 背景
  ctx.fillStyle = '#1a1a2e'
  ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)

  // 网格线
  ctx.strokeStyle = 'rgba(255,255,255,0.03)'
  ctx.lineWidth = 0.5
  for (let x = 0; x <= COLS; x++) {
    ctx.beginPath()
    ctx.moveTo(x * CELL_SIZE, 0)
    ctx.lineTo(x * CELL_SIZE, CANVAS_SIZE)
    ctx.stroke()
  }
  for (let y = 0; y <= ROWS; y++) {
    ctx.beginPath()
    ctx.moveTo(0, y * CELL_SIZE)
    ctx.lineTo(CANVAS_SIZE, y * CELL_SIZE)
    ctx.stroke()
  }

  // 迷宫障碍物
  if (gameMode.value === 'maze') {
    ctx.fillStyle = '#4a4a6a'
    MAZE_WALLS.forEach(k => {
      const [x, y] = k.split(',').map(Number)
      ctx.fillRect(x * CELL_SIZE + 1, y * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2)
    })
  }

  // 炸弹
  for (const bomb of bombs) {
    const cx = bomb.x * CELL_SIZE + CELL_SIZE / 2
    const cy = bomb.y * CELL_SIZE + CELL_SIZE / 2
    const r = CELL_SIZE / 2 - 3
    ctx.fillStyle = '#333'
    ctx.beginPath()
    ctx.arc(cx, cy, r, 0, Math.PI * 2)
    ctx.fill()
    // 引线
    ctx.strokeStyle = '#e74c3c'
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.moveTo(cx + r * 0.6, cy - r * 0.8)
    ctx.lineTo(cx + r * 0.6, cy - r - 3)
    ctx.stroke()
    // 火花
    ctx.fillStyle = '#f39c12'
    ctx.beginPath()
    ctx.arc(cx + r * 0.6, cy - r - 4, 2.5, 0, Math.PI * 2)
    ctx.fill()
  }

  // 普通食物（金色圆形 + 光晕）
  if (food) {
    const cx = food.x * CELL_SIZE + CELL_SIZE / 2
    const cy = food.y * CELL_SIZE + CELL_SIZE / 2
    ctx.fillStyle = 'rgba(241, 196, 15, 0.3)'
    ctx.beginPath()
    ctx.arc(cx, cy, CELL_SIZE / 2, 0, Math.PI * 2)
    ctx.fill()
    ctx.fillStyle = '#f1c40f'
    ctx.beginPath()
    ctx.arc(cx, cy, CELL_SIZE / 2 - 3, 0, Math.PI * 2)
    ctx.fill()
  }

  // 红色食物（红色圆形 + 光晕）
  if (redFood) {
    const cx = redFood.x * CELL_SIZE + CELL_SIZE / 2
    const cy = redFood.y * CELL_SIZE + CELL_SIZE / 2
    const pulse = Math.sin(Date.now() / 300) * 0.2 + 0.3
    ctx.fillStyle = `rgba(231, 76, 60, ${pulse})`
    ctx.beginPath()
    ctx.arc(cx, cy, CELL_SIZE / 2, 0, Math.PI * 2)
    ctx.fill()
    ctx.fillStyle = '#e74c3c'
    ctx.beginPath()
    ctx.arc(cx, cy, CELL_SIZE / 2 - 2, 0, Math.PI * 2)
    ctx.fill()
  }

  // 蛇身
  snake.forEach((seg, i) => {
    const x = seg.x * CELL_SIZE
    const y = seg.y * CELL_SIZE
    const pad = 1

    if (i === 0) {
      // 蛇头
      ctx.fillStyle = '#2ecc71'
      ctx.fillRect(x + pad, y + pad, CELL_SIZE - pad * 2, CELL_SIZE - pad * 2)
      // 眼睛：根据方向计算两只眼睛的位置
      const cx = x + CELL_SIZE / 2
      const cy = y + CELL_SIZE / 2
      let e1x, e1y, e2x, e2y, puOffX = 0, puOffY = 0
      if (direction.x === 1) {       // 向右：眼在上右和下右
        e1x = cx + 7; e1y = cy - 5;  e2x = cx + 7; e2y = cy + 5;  puOffX = 1
      } else if (direction.x === -1) { // 向左：眼在上左和下左
        e1x = cx - 7; e1y = cy - 5;  e2x = cx - 7; e2y = cy + 5;  puOffX = -1
      } else if (direction.y === -1) { // 向上：眼在左上和右上
        e1x = cx - 5; e1y = cy - 7;  e2x = cx + 5; e2y = cy - 7;  puOffY = -1
      } else {                          // 向下：眼在左下和右下
        e1x = cx - 5; e1y = cy + 7;  e2x = cx + 5; e2y = cy + 7;  puOffY = 1
      }
      ctx.fillStyle = '#fff'
      ctx.beginPath(); ctx.arc(e1x, e1y, 3.5, 0, Math.PI * 2); ctx.fill()
      ctx.beginPath(); ctx.arc(e2x, e2y, 3.5, 0, Math.PI * 2); ctx.fill()
      ctx.fillStyle = '#000'
      ctx.beginPath(); ctx.arc(e1x + puOffX, e1y + puOffY, 1.5, 0, Math.PI * 2); ctx.fill()
      ctx.beginPath(); ctx.arc(e2x + puOffX, e2y + puOffY, 1.5, 0, Math.PI * 2); ctx.fill()
    } else {
      // 蛇身渐变
      const ratio = 1 - i / (snake.length + 10)
      const g = Math.floor(100 + ratio * 155)
      ctx.fillStyle = `rgb(30, ${g}, 60)`
      ctx.fillRect(x + pad, y + pad, CELL_SIZE - pad * 2, CELL_SIZE - pad * 2)
    }
  })
}

// ==================== 键盘事件 ====================
function handleKeydown(e) {
  if (gameState.value === 'idle') return
  // 忽略修饰键组合（Ctrl/Alt/Meta+方向键等）
  if (e.ctrlKey || e.altKey || e.metaKey) return

  const keyMap = {
    ArrowUp:    { x: 0, y: -1 },
    ArrowDown:  { x: 0, y: 1 },
    ArrowLeft:  { x: -1, y: 0 },
    ArrowRight: { x: 1, y: 0 },
  }

  if (keyMap[e.key]) {
    e.preventDefault()
    const nd = keyMap[e.key]
    // 禁止 180° 掉头（检测已排队的 nextDirection，而非当前 direction）
    if (nd.x === -nextDirection.x && nd.y === -nextDirection.y && snake.length > 1) return
    nextDirection = nd
  }

  if (e.key === ' ') {
    e.preventDefault()
    speedBoost = true
  }

  if (e.key === 'Enter') {
    e.preventDefault()
    if (e.repeat) return // 忽略长按重复事件
    if (gameState.value === 'playing') {
      gameState.value = 'paused'
    } else if (gameState.value === 'paused') {
      resumeGame()
    }
  }
}

function handleKeyup(e) {
  if (e.key === ' ') {
    e.preventDefault()
    speedBoost = false
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  // 读取最高分（容错处理：隐私模式 / 存储满 / 数据损坏）
  try {
    const saved = localStorage.getItem('snake-high-score')
    if (saved) {
      const n = parseInt(saved, 10)
      highScore.value = Number.isFinite(n) ? n : 0
    }
  } catch {}
  // 初始化 idle 状态下的画布（仅绘制一次，不需要持续循环）
  const startX = Math.floor(COLS / 2)
  const startY = Math.floor(ROWS / 2)
  snake = [
    { x: startX, y: startY },
    { x: startX - 1, y: startY },
    { x: startX - 2, y: startY },
    { x: startX - 3, y: startY },
  ]
  draw()
  // 将 keyup 绑定在 window 上，避免 canvas 失焦后 Space 松开事件丢失
  window.addEventListener('keyup', handleKeyup)
})

onUnmounted(() => {
  clearInterval(countdownInterval)
  if (animationId) cancelAnimationFrame(animationId)
  window.removeEventListener('keyup', handleKeyup)
})
</script>

<style scoped>
.snake-game-page {
  max-width: 700px;
}

.settings-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.setting-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.setting-group .label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.score-display {
  font-size: 14px;
  color: #333;
}

.canvas-wrapper {
  position: relative;
  display: inline-block;
  outline: none;
}

.canvas-wrapper canvas {
  display: block;
  border-radius: 4px;
  outline: none;
}

.pause-overlay,
.gameover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  font-size: 18px;
  border-radius: 4px;
  pointer-events: none;
}

.gameover-overlay {
  pointer-events: auto;
}
</style>
