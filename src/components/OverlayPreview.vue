<script setup lang="ts">
import type { CSSProperties } from 'vue'
import { toolCatalog } from '../config/tools'
import AppIcon from './AppIcon.vue'

const style = Object.fromEntries(
  Object.entries(toolCatalog.toolbar).map(([key, value]) => [
    `--toolbar-${key.replace(/[A-Z]/g, (letter) => `-${letter.toLowerCase()}`)}`,
    `${value}px`,
  ]),
) as CSSProperties
</script>

<template>
  <div class="overlay-preview" :style="style" aria-label="悬浮窗预览">
    <div class="preview-diagram">
      <span class="callout-label">{{ toolCatalog.actions[0].label }}</span>
      <AppIcon name="callout" class="callout" />
      <div class="floating-toolbar" aria-hidden="true">
        <div v-for="action in toolCatalog.actions" :key="action.id" class="preview-button">
          <AppIcon :name="action.icon" />
        </div>
      </div>
      <AppIcon name="callout" class="callout reverse" />
      <span class="callout-label">{{ toolCatalog.actions.at(-1)?.label }}</span>
    </div>
  </div>
</template>

<style scoped>
.overlay-preview {
  height: 96px;
  margin-bottom: 8px;
  display: grid;
  place-items: center;
  background: var(--subtle);
  border-radius: 8px;
}
.preview-diagram {
  display: flex;
  align-items: center;
}
.callout-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text);
}
.callout {
  width: 24px;
  height: 24px;
  margin: 0 6px;
  color: var(--muted);
}
.reverse {
  transform: scaleX(-1);
}
.floating-toolbar {
  display: flex;
  align-items: center;
  padding: var(--toolbar-padding);
  border-radius: var(--toolbar-radius);
  background: var(--surface);
  box-shadow:
    inset 0 0 0 1px var(--line),
    0 2px 5px #00000012;
}
.preview-button {
  width: var(--toolbar-button-size);
  height: var(--toolbar-button-size);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.preview-button + .preview-button {
  margin-left: calc(var(--toolbar-separator-width) + var(--toolbar-separator-margin) * 2);
}
.preview-button + .preview-button::before {
  content: '';
  position: absolute;
  right: calc(100% + var(--toolbar-separator-margin));
  width: var(--toolbar-separator-width);
  height: var(--toolbar-separator-height);
  background: var(--line);
}
.preview-button .app-icon {
  width: var(--toolbar-icon-size);
  height: var(--toolbar-icon-size);
  color: var(--text);
}
</style>
