import eslint from '@eslint/js'
import eslintConfigPrettier from 'eslint-config-prettier'
import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'

export default tseslint.config(
  {
    ignores: ['dist/**', 'node_modules/**', 'playwright-report/**', 'test-results/**']
  },
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      parser: tseslint.parser,
      globals: {
        HTMLDialogElement: 'readonly'
      }
    }
  },
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: pluginVue.parser,
      globals: {
        HTMLDialogElement: 'readonly'
      },
      parserOptions: {
        parser: tseslint.parser
      }
    }
  },
  {
    files: ['**/*.{ts,tsx,vue}'],
    rules: {
      // Vueの単一ファイルコンポーネント名は既存の役割名を優先する。
      'vue/multi-word-component-names': 'off'
    }
  },
  {
    files: ['src/App.vue', 'src/components/**/*.vue'],
    rules: {
      // UI層の通信処理をAPIクライアントへ集約し、コンポーネントからの直接通信を禁止する。
      'no-restricted-syntax': [
        'error',
        {
          selector: "CallExpression[callee.name='fetch']",
          message: 'API通信はsrc/services/taskApi.tsへ集約してください。'
        }
      ]
    }
  },
  {
    files: ['**/*.test.{ts,tsx}', 'e2e/**/*.ts'],
    rules: {
      // テストランナーが提供するグローバルAPIは型チェックで検証する。
      'no-undef': 'off'
    }
  },
  eslintConfigPrettier
)
