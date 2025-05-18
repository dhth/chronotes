alias r := dev
alias b := build
alias f := fmt
alias fc := fmt-check

dev:
  bun run dev

build:
  bun run build

fmt:
  scalafmt src/main/scala

fmt-check:
  scalafmt src/main/scala --check
