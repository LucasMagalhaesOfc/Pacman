# PAC-MAN HALLOWEEN — V5.5

**Esta e a versao 5.5 completa do projeto LibGDX (Java), construida sobre a V5.** Nao e apenas uma ilustracao: os arquivos Java, mapa TXT, sprites e controlador foram alterados.

## Novidades

- **Seis fantasmas**, nao quatro. Dois novos sprites (rosa e dourado) e seus spawns foram adicionados ao `assets/maps/mapa_v5.txt`. Eles continuam respeitando paredes e nao podem escolher o mesmo bloco ocupado por outro fantasma.
- **Fuga ao comer a bolinha vermelha:** os seis ficam assustados, ganham aparencia ciano com olhos de medo e procuram se **afastar** do Pac-Man, usando distancias reais ao longo dos corredores. Correm um pouco mais rapido (intervalo de passo reduzido para 77%). Depois do poder de 8 s, voltam a perseguir/patrulhar normalmente. A perseguicao continua limitada a **224 px do mapa**; durante o modo monstro, a fuga e global, para todos reagirem.
- **Chapeu cai na transformacao:** no modo monstro o Pac-Man aparece com grandes chifres, olhos raivosos e dentes; o chapeu se desprende do ponto em que a bolinha foi comida, cai girando e desaparece. Ao voltar ao normal, ele recupera o chapeu. Outra bolinha vermelha durante o mesmo poder apenas renova o tempo, sem reiniciar repetidamente a queda.
- **Tela de nome obrigatoria:** a cada abertura e a cada nova partida, digite um nome (ate 16 caracteres), `ENTER` para comecar. `BACKSPACE` apaga. Caracteres acentuados sao convertidos para letras sem acento para funcionar com a fonte de pixel da interface.
- **Pausa em P:** congela a partida, incluindo tempo, movimento dos seis fantasmas, duracao do poder e contagem de respawn. A musica tambem pausa. Aperte `P` novamente para continuar. Ao perder foco, o jogo pausa automaticamente.
- **Vitoria por limpar todas as bolinhas:** bolinhas normais e vermelhas devem acabar; a cereja e um bonus opcional. A tela de `VITORIA!` aparece e a partida passa a constar no ranking. A vitoria e verificada antes da colisao tardia de um fantasma no quadro final.
- **Ranking local persistente:** armazena **apenas partidas vencidas**, com nome, pontos e tempo. A lista e ordenada por `pontos / minutos jogados` (maior e melhor); em empates, maior pontuacao e menor tempo vencem. O painel exibe os oito melhores resultados e preserva ate 100 resultados na preferencia local `pacman-halloween-v55` do LibGDX. O ranking reaparece ao reabrir o jogo. Nao depende de servidor, internet ou conta.
- **Novo jogo em R** depois de vitoria ou derrota volta para a tela de nome, sem apagar o ranking. O recorde historico de pontos da V5 tambem continua salvo na preferencia original `pacman`.

## Teclas

| Tecla | Funcao |
|---|---|
| `WASD` / setas | Movimentar (a tecla direcional pressionada mais recentemente tem prioridade) |
| `P` | Pausar / continuar |
| `ENTER` | Iniciar partida apos digitar nome |
| `BACKSPACE` | Corrigir nome |
| `R` | Nova partida apos vitoria ou derrota; pede um novo nome |

## Como executar no IntelliJ (Windows)

1. Extraia `pacman-github-halloween-v5.5.zip` e abra **a pasta `pacman-github`** no IntelliJ IDEA.
2. Deixe o Gradle sincronizar as dependencias do LibGDX. Configure uma JDK compativel com o Gradle Wrapper do projeto; a sincronizacao inicial exige conexao com a internet se as dependencias nao estiverem em cache.
3. Execute `org.pacman.lwjgl3.Lwjgl3Launcher` ou use na pasta raiz:

```bat
gradlew.bat lwjgl3:run
```

4. Digite o nome na tela inicial e pressione `ENTER`. A janela foi configurada para 920 x 980 pixels.

## Arquivos principais

| Objetivo | Local |
|---|---|
| Menu, pausa, cronometro, inicio, reinicio e registro da vitoria | `core/src/main/java/org/pacman/Main.java` |
| Ranking e criterio de ordenacao | `core/src/main/java/org/pacman/model/RankingPartidas.java` |
| Regra de vitoria, vidas, coleta e seis fantasmas | `core/src/main/java/org/pacman/controller/ControladorJogo.java` |
| Fuga, patrulha, colisao entre fantasmas e alcance de visao | `core/src/main/java/org/pacman/model/Fantasma.java` |
| Animacao da transformacao e queda do chapeu | `core/src/main/java/org/pacman/model/Pacman.java` e `view/RenderizadorJogo.java` |
| Paredes, corredores e seis pontos de spawn | `assets/maps/mapa_v5.txt` |
| Arte do mapa (apenas visual, nao e colisao) | `assets/images/mapa_halloween_v5.png` |
| Sprites novos | `assets/images/*v55.png` |
| Script para recriar os novos sprites | `tools/gerar_sprites_v55.py` |

**Importante sobre o mapa:** a estrutura TXT continua sendo a fonte de verdade para as paredes; o PNG e apenas o cenario, impedindo o antigo bug de ficar preso em paredes de pixels. Para regenerar o labirinto, `tools/gerar_arte_v5.py` ja foi atualizado para manter os seis spawns. Para recriar os visuais do modo monstro e dos fantasmas, use depois `tools/gerar_sprites_v55.py`. Esses scripts exigem Python e Pillow **apenas para desenvolvimento**; o jogo executado nao precisa deles.

## Testes realizados

- **Sete grupos V5.5** rodando com `javac` sem dependencias externas: geometria de corredor unico, seis spawns, conectividade, queda de chapeu e transformacao, fuga por corredor, 12 mil quadros dos seis fantasmas e algoritmo de ranking/persistencia. Executar em Windows com `tests\testar_logica_v55.bat`.
- **Integracao com substitutos locais do LibGDX:** fontes `Main`, controlador e renderizador compiladas com os contratos simulados da API, e a aplicacao exercitada em inicializacao, input, tela de nome, 300 quadros de pause, retomada, fim de jogo, vitoria registrada uma vez, ranking recarregado e nova partida. Executar `tests\testar_integracao_mock_v55.bat` (apenas testes simulados).
- **Limitacao:** a compilacao real pelo Gradle e a execucao grafica OpenGL nao puderam ser concluidas aqui. `./gradlew :core:compileJava --offline` falhou ao tentar obter a distribuicao Gradle 9.8.0 por `UnknownHostException` (`services.gradle.org`). Portanto, a V5.5 deve ser compilada e testada graficamente no seu IntelliJ para validar animacoes, som e controles com as bibliotecas reais.

`PREVIA_PACMAN_HALLOWEEN_V55.png` e uma composicao ilustrativa dos novos arquivos; **nao** e uma captura de execucao real. A pontuacao e o tempo que aparecem na previa sao apenas exemplos.
