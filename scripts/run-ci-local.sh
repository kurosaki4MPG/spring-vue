#!/usr/bin/env sh

set -eu

job="${1:-all}"
case "$job" in
  all|backend|frontend) ;;
  *)
    echo "Usage: $0 [all|backend|frontend]" >&2
    exit 2
    ;;
esac

# Run act from the repository root and reproduce the GitHub Actions push event.
script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repository_root="$(cd "$script_dir/.." && pwd)"
cd "$repository_root"

if command -v act >/dev/null 2>&1; then
  act_command="$(command -v act)"
else
  # Support Winget installs when Git Bash has not refreshed its PATH yet.
  act_command=""
  user_name="${USERNAME:-${USER:-}}"
  winget_act_pattern="/c/Users/$user_name/AppData/Local/Microsoft/WinGet/Packages/nektos.act_*/act.exe"
  for candidate in $winget_act_pattern; do
    if [ -f "$candidate" ]; then
      act_command="$candidate"
      break
    fi
  done
fi

if [ -z "$act_command" ]; then
  echo "act was not found. Install act before running this script." >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI was not found. Start Docker before running this script." >&2
  exit 1
fi

# Preserve the executable bit when act copies the local workspace into its job container.
chmod +x backend/mvnw

if [ "$job" = "all" ]; then
  "$act_command" push
else
  "$act_command" push -j "$job"
fi
