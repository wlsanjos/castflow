# CastFlow

CastFlow é um app Android open-source para descobrir e transmitir fotos e vídeos para TVs (foco inicial em dispositivos Samsung/Tizen).

## Por que eu criei este app

Desenvolvi este aplicativo porque estou cansado de instalar vários apps desse mesmo segmento, ter que assistir vários anúncios e, no fim, ter que pagar pelo uso; por isso nasceu este aplicativo.

## Objetivo

- Descobrir TVs na mesma rede local (SSDP/mDNS)
- Parear/conectar com a TV (WebSocket / protocolo Samsung Remote)
- Permitir que o usuário selecione mídia da galeria e, futuramente, transmita para a TV

## Status atual

- UI moderna em Jetpack Compose (Material 3)
- Implementação real de descoberta (SSDP + mDNS) e conexão WebSocket para handshakes com TVs Samsung
- Mock disponível para desenvolvimento rápido
- Fluxo básico: Discover → Connect → Library (após conexão)

## Como rodar localmente

Requisitos:

- JDK 17+, Android SDK (com Android 13 API para permissões de mídia)
- Gradle (embutido no wrapper)

Build e instalação (na raiz do projeto):

```bash
./gradlew :app:assembleDebug --stacktrace
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.wlsanjos.castflow/.MainActivity
```

Debug Discover (útil para desenvolvimento):

```bash
adb shell am start -n com.wlsanjos.castflow/.debug.DebugDiscoverActivity
```

## Contribuindo

- Sugestões rápidas:
  - Adicione uma issue descrevendo o motivo e o escopo da mudança.
  - Abra um pull request com um branch descritivo (ex: `feat/discovery-ssdp-improve`).
  - Siga mensagens de commit semântico: `feat:`, `fix:`, `docs:`, `chore:`, `refactor:` etc.

- Arquivos úteis a adicionar ao repo:
  - `LICENSE` (recomendo MIT para permissividade)
  - `CONTRIBUTING.md` com guia de estilo, como rodar testes e branch/review flow
  - `CODE_OF_CONDUCT.md` (opcional, recomendado para projetos públicos)

## Tagging e releases

Este repositório usa tags Git para releases. Exemplo (local):

```bash
git tag -a v0.1.0 -m "v0.1.0 - initial release"
git push origin main --tags   # (execute quando quiser publicar)
```

## Publicar APK no GitHub Releases

- Configure um workflow GitHub Actions que execute `./gradlew assembleRelease` (ou `assembleDebug`), e use `actions/upload-release-asset` para anexar o APK ao release.
- Lembre-se: para builds de release, gere uma chave de assinatura (keystore) e adicione variáveis seguras no GitHub (se for assinar automaticamente).

## Licença

Recomendo `MIT` se quiser máxima permissividade para quem usar o app. Se preferir proteger alterações derivadas, escolha `GPLv3`.

## Próximos passos sugeridos

1. Adicionar `LICENSE` (MIT) e `CONTRIBUTING.md`.
2. Implementar prompt de permissões de mídia runtime na `LibraryScreen`.
3. Configurar GitHub Actions para gerar APKs automaticamente e anexar a Releases.
4. Documentar o protocolo de conexão (observações e limitações com modelos Samsung específicos).

## Contato

Se quiser ajuda com os próximos passos (CI, publicação, templates), eu posso guiar e gerar os arquivos necessários.

---

CastFlow — criado com foco na simplicidade e sem anúncios invasivos.
