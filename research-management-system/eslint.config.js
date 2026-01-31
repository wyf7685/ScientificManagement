import pluginVue from 'eslint-plugin-vue'
import parserVue from 'vue-eslint-parser'
import parserTs from '@typescript-eslint/parser'
import pluginTs from '@typescript-eslint/eslint-plugin'

export default [
  {
    ignores: ['dist/**', 'dist-ssr/**', 'coverage/**', 'node_modules/**']
  },
  {
    files: ['**/*.{js,mjs,cjs,vue}'],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      parser: parserVue,
      parserOptions: {
        ecmaVersion: 2022,
        sourceType: 'module',
        parser: parserTs,
        extraFileExtensions: ['.vue']
      },
    },
    plugins: {
      vue: pluginVue,
      '@typescript-eslint': pluginTs,
    },
    rules: {
      ...pluginVue.configs['flat/recommended'].rules,
      'vue/multi-word-component-names': 'off'
    }
  }
]
