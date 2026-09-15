# FavelaAddons

Mod cliente para Fabric, Minecraft 26.1.2, feito para o servidor Telos Realms.

## Features

**QOL**

- Spacebar Spammer
- Alerts por palavra no chat, com texto e cor configuráveis
- Medidor de DPS
- Sons por gatilho de chat
- Contador de traps, com bloqueio do clique direito no limite
- Calls de Ambush e Deathmark por porcentagem de vida do boss
- Porcentagem de vida do boss na tela

**Livesplits**

Cronometragem de dungeons no estilo LiveSplit, com fases de boss como sub-splits.

- Início automático da run pelo portal, descontando o tempo já decorrido no contador do portal
- Splits por mensagem de chat, por porcentagem de vida do boss ou por portal
- Comparação com o personal best a cada checkpoint, com as cores do LiveSplit
- Gold splits por segmento
- Fases do boss recolhem quando ele morre

Todos os elementos de tela são arrastáveis por `/fa editarHUD`.

## Comandos

| Comando | O que faz |
| --- | --- |
| `/fa` | Abre o menu de configuração |
| `/fa help` | Lista os comandos |
| `/fa editarHUD` | Move os textos na tela |
| `/fa testar` | Testa o texto e o som do alerta |
| `/fa som` | Testa o som do alerta |
| `/fa split` | Fecha o segmento atual |
| `/fa split delete` | Desfaz o último split |
| `/fa split cancel` | Cancela a run atual |
| `/fa split reset` | Apaga os personal bests |
| `/fa split status` | Mostra o cue que o mod está esperando |
| `/fa splits` | Recarrega e lista as rotas |
| `/fa splits pb [dungeon]` | Mostra o personal best |
| `/fa splits iniciar <dungeon>` | Inicia uma run manualmente |
| `/fa debug` | Liga os logs de diagnóstico |

## Configuração

Fica em `config/favelaaddons/`:

- `favelaaddons.json` — opções do menu
- `splits.json` — rotas, segmentos e cues do Livesplits
- `pb.json` — personal bests e golds

As rotas do Livesplits são editáveis: cada dungeon tem uma lista de segmentos, e cada segmento fecha por um cue de chat (`chat`), por um portal (`portal`), por porcentagem de vida (`hp`) ou pelo fim da boss bar (`bossKill`). Sub-splits vão no campo `children`.

## Build

Não usa Gradle. Compila com `javac --release 21` contra uma cópia do jar do Minecraft com o access widener aplicado, e é empacotado com `jar`.
