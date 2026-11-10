# Correcao V5.5.1 — erro de compilacao do chapeu

Na V5.5, a chamada `SpriteBatch.draw(texturaChapeu, x, y, origemX, origemY, largura, altura, escalaX, escalaY, rotacao)` nao compila com o LibGDX real: essa assinatura de 9 parametros numericos requer `TextureRegion`, nao `Texture`.

**Alterado:** `core/src/main/java/org/pacman/view/RenderizadorJogo.java`

- Adicionado campo `TextureRegion regiaoChapeu`.
- Inicializado uma unica vez usando `new TextureRegion(texturaChapeu)`.
- A chamada da animacao usa `batch.draw(regiaoChapeu, ...)`, mantendo a queda e a rotacao do chapeu.
- Os stubs da API no teste de integracao foram corrigidos para remover a assinatura inexistente de `draw(Texture, ..., rotation)`, evitando que o mesmo erro passe despercebido novamente.

A logica do jogo, o mapa, os sprites e o ranking da V5.5 foram preservados. Validamos a compilacao com mocks mais fieis ao LibGDX e os testes logicos da V5.5. O build real do Gradle e a execucao grafica ainda precisam ser conferidos no computador do usuario.
