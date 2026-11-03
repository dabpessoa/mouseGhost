Stack: Java 25 + Spring Boot 4.0.5 + JavaFX 25 + AWT Robot + Maven.

Descrição: Aplicação desktop que detecta ociosidade e realiza movimentos de mouse orgânicos (curvas de Bézier + smoothstep) para manter sessões ativas.



Camada -> Componentes Principais -> Observação
Bootstrap -> RunApplication e JavaFXApplication -> Spring Boot iniciado dentro do ciclo de vida do JavaFX
Serviço de domínio -> MouseService, MouseMonitor, MouseMoveRunnable -> Lógica de ociosidade + movimento
UI -> Controllers FXML + AbstractView / MainView -> Navegação por troca de conteúdo em Pane
Persistência de config -> SettingsFileUtils + spring.config.import -> Arquivo em %APPDATA%/MouseGhost/settings.properties
Infra -> Tray, logging em TextArea + arquivo, ícones -> Bem implementado


jpackage --type app-image --name MeuApp --input input --main-jar mouseghost-1.0.0.jar --main-class org.springframework.boot.loader.launch.JarLauncher --dest dist

ftype jarfile="C:\Program Files\Java\jdk-25.0.2+10\bin\javaw.exe" -jar "%1" %*

