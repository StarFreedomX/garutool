import { spawnSync } from 'node:child_process'
import { copyFile, mkdir, readFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { join } from 'node:path'

const root = fileURLToPath(new URL('..', import.meta.url))
const lint = process.argv.includes('--lint')
const result = spawnSync('./gradlew', [lint ? ':app:lintDebug' : ':app:assembleDebug'], {
  cwd: join(root, 'android'),
  stdio: 'inherit',
})
if (result.error) throw result.error
if (result.status !== 0) process.exit(result.status ?? 1)
if (!lint) {
  const { version } = JSON.parse(await readFile(join(root, 'package.json'), 'utf8'))
  await mkdir(join(root, 'dist'), { recursive: true })
  const apk = join(root, 'dist', `GaruTool-${version}.apk`)
  await copyFile(join(root, 'android/app/build/outputs/apk/debug/app-debug.apk'), apk)
  console.log(apk)
}
