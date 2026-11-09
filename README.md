# Mouse Ghost

Aplicativo desktop para Windows que detecta períodos de inatividade e movimenta o cursor suavemente para manter a sessão ativa.

O monitor trabalha localmente com Java AWT `Robot`. Não há servidor web, telemetria ou coleta de dados. Os links de apoio ao projeto abrem somente quando o usuário escolhe uma opção de doação.

## Como usar

1. Abra o aplicativo e confira o tempo de ociosidade em **Configurações**.
2. Clique em **Iniciar monitor**.
3. Acompanhe o estado e a contagem regressiva no cartão de status.
4. Use **Ocultar na bandeja** para manter o monitor em segundo plano.
5. Clique em **Parar** ou use a opção equivalente na bandeja para encerrar o monitoramento.

O movimento é interrompido quando o usuário retoma o controle do mouse. O aplicativo também respeita o monitor em que o cursor está localizado e considera configurações de múltiplos monitores.

Por padrão, digitar também é considerado atividade e reinicia o cronômetro. Em **Configurações**, ative **Ignorar teclado** se quiser que somente movimentos do mouse sejam considerados.

## Desenvolvimento

Requisitos:

- JDK 25;
- Maven 3.9 ou superior;
- Windows para executar os recursos de AWT `Robot` e gerar o pacote nativo.

Comandos principais:

```powershell
mvn test
mvn clean package -Pwin-exe
```

O workflow `Verify` executa testes e valida o pacote em Pull Requests e na branch principal. O workflow de release gera o aplicativo Windows quando uma tag `v*` é publicada.

## Organização

- `service`: monitoramento, movimento e bandeja do sistema;
- `model`: estado e fotografias imutáveis para a interface;
- `view`: telas JavaFX e controllers;
- `utils`: persistência de configurações, logging e recursos auxiliares.

Página do projeto: https://dabpessoa.github.io/mouseGhost/
