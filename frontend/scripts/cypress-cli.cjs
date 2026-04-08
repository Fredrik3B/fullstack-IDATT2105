/* global process, require */

const { spawnSync } = require('node:child_process')
const path = require('node:path')

const args = process.argv.slice(2)

const env = { ...process.env }
delete env.ELECTRON_RUN_AS_NODE

const cypressCli = path.join(process.cwd(), 'node_modules', 'cypress', 'bin', 'cypress')

const result = spawnSync(process.execPath, [cypressCli, ...args], {
  stdio: 'inherit',
  env,
})

if (result.error) {
  throw result.error
}

process.exit(result.status ?? 1)
