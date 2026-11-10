# PAC-MAN HALLOWEEN — V5

## A versao entregue

A V5 foi feita **sobre o projeto Java + LibGDX** da V4 e mantem os modulos `core` e `lwjgl3`, o Gradle Wrapper, as quatro cores de fantasma, o modo monstro, a cereja, vidas, sons e recorde.

**Alteracoes principais**

1. **Corredores de uma celula:** o arquivo `assets/maps/mapa_v5.txt` determina todas as paredes, caminhos, coletaveis e spawns. A imagem nao decide mais colisoes. O carregador verifica corredores de uma celula de largura e rejeita regioes 2x2 abertas, evitando multiplas fileiras de bolinhas.
2. **Mapa simplificado:** a arte `assets/images/mapa_halloween_v5.png` tem cores escuras, paredes roxas iluminadas e pequenos detalhes de Halloween. Sem objetos decorativos interferindo no caminho. O mapa novo tem **25 colunas x 29 linhas**, **383 celulas caminhaveis** e **163 itens** (quatro especiais + uma cereja).
3. **Pac-Man que nao prende na parede:** o movimento agora ocorre por centros de celulas da propria grade textual. Nenhum reposicionamento prematuro em curvas e nenhuma amostragem de pixels para validar passagens. Reversao exata, curvas em buffer e parada no centro da ultima celula livre.
4. **Controles corrigidos:** segurar W e pressionar D nao deixa W ganhar prioridade: a tecla nova vence enquanto estiver pressionada.
5. **Novo visual do Pac-Man:** duas spritesheets em 48px (normal e monstro), quatro animacoes de boca e quatro direcoes. Chapéu fica em pe mesmo na virada; modo monstro tem olhos agressivos, presas e aura avermelhada.
6. **Fantasmas sem engarrafamento infinito:** cada fantasma reserva seu quadrado, evita quadrados ocupados / ainda sendo desocupados, espera brevemente ou desvia. O efeito antigo de tontura por bater em outro fantasma foi removido, pois causava loops de colisao. Os fantasmas se movem com interpolacao visual.
7. **Campo de visao em pixels:** cada fantasma so inicia perseguicao quando o Pac-Man esta a ate **224px no mapa** (sete celulas de 32px), com caminho pela grade; longe disso, patrulha sem perseguir globalmente.
8. **Mecanicas mantidas e ajustadas:** três vidas, 8s de modo monstro, 8s para fantasma devorado renascer, combos 200/400/800/1600, invulnerabilidade breve apos respawn. Colisao Pac-Man/fantasma agora usa hitbox de 0,63 celula, nao duas celulas como na V4. **R** reinicia apos vitoria ou Game Over. O recorde nao escreve em disco a cada bolinha.

## Como abrir e executar no Windows

1. Extraia o ZIP e abra a **pasta `pacman-github`** no IntelliJ IDEA.
2. Tenha um JDK compativel com o Gradle Wrapper do projeto (a V4 usa Gradle **9.8.0**), configure o Gradle JVM no IntelliJ e espere a sincronizacao das dependencias na primeira abertura com internet.
3. Execute `org.pacman.lwjgl3.Lwjgl3Launcher` **ou** no terminal, na pasta `pacman-github`, rode:

   ```bat
   gradlew.bat lwjgl3:run
   ```

4. A janela abre em **920 x 980**. Use **setas ou WASD**. Depois de vencer ou morrer, pressione **R**.

## Validacao feita para a V5

- Testes Java automatizados **sem LibGDX**: 7 grupos de testes de geometria e logica, incluindo **672 tentativas contra parede, 494 curvas, 860 reversoes, 20.000 quadros de movimento do Pac-Man e 10.000 quadros de quatro fantasmas**, sem ultrapassar paredes ou ocupar a mesma celula.
- O codigo completo do modulo `core` foi compilado contra **substitutos locais das assinaturas LibGDX** e a inicializacao/ciclo de jogo foram simulados por 900 quadros. O bug de prioridade de teclas foi testado separadamente.
- **Ainda precisa rodar o Gradle real e jogar a V5 no seu computador.** O ambiente de preparacao nao tinha as dependencias Gradle/LibGDX ja baixadas e a tentativa de baixar o wrapper falhou por DNS. Nao afirmamos que houve teste grafico em OpenGL nem que a compilacao real com LibGDX foi concluida.

Rode os testes de logica em Windows com `tests\testar_logica_v5.bat` (requer JDK/javac) ou veja o arquivo `tests/V5SmokeTests.java`.

## Onde ajustar depois

| Objetivo | Arquivo |
|---|---|
| Mapa e itens | `assets/maps/mapa_v5.txt` e `tools/gerar_arte_v5.py` |
| Visual do labirinto | `assets/images/mapa_halloween_v5.png` |
| Movimento do jogador | `core/src/main/java/org/pacman/model/Pacman.java` |
| Teclas | `core/src/main/java/org/pacman/controller/ManipuladorEntrada.java` |
| Alcance da visao dos fantasmas | `model/Fantasma.java`, constante `RAIO_VISAO_PIXELS = 224` |
| IA e desviamento dos fantasmas | `model/Fantasma.java` e `ai/EstrategiaPerseguicao.java` |
| Tamanho visual, animacoes e HUD | `view/RenderizadorJogo.java` |
| Spawns, pontos, vidas, win/lose | `controller/ControladorJogo.java` |

**Importante:** se quiser redesenhar os corredores, altere o script `tools/gerar_arte_v5.py` e execute-o para regenerar **TXT e PNG juntos**. O PNG e uma textura de fundo; editar somente a textura sem editar o mapa TXT pode desalinha-los visualmente. O script exige Python com Pillow; **o jogo em si nao exige Python**.

A imagem `PREVIA_PACMAN_HALLOWEEN_V5.png` e uma **composicao ilustrativa montada com os assets finais**, nao uma captura do jogo rodando.
