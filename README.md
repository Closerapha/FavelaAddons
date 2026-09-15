# FavelaAddons

Mod cliente para Fabric, Minecraft 26.1.2, feito para o servidor Telos Realms.

Versão reduzida, com duas abas de configuração:

- **QOL** — Spacebar Spammer, Alerts, DPS, Sounds, Trap Counter, Ambush/Deathmark, Boss HP
- **Livesplits** — cronometragem de dungeons e bosses no estilo LiveSplit

A versão completa, com as abas Assists e DEV, é o [FavelaClient](https://github.com/Closerapha/FavelaClient).

## Comandos

| Comando | O que faz |
| --- | --- |
| `/fa` | Abre o menu de configuração |
| `/fa help` | Lista os comandos |
| `/fa editarHUD` | Move os textos na tela |
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

Fica em `config/sapo/`:

- `sapo.json` — opções do menu
- `splits.json` — rotas, segmentos e cues do Livesplits
- `pb.json` — personal bests e golds

## Build

Não usa Gradle. Compila com `javac --release 21` contra uma cópia do jar do Minecraft com o access widener aplicado, e é empacotado com `jar`.

A constante `com.sapo.Build.FULL` separa as duas versões: com `false`, as abas Assists e DEV não são criadas e as features delas não são registradas.
