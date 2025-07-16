# Boleiragem

![Logo do Aplicativo](app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

## 📱 Sobre o Projeto

Boleiragem é um aplicativo Android desenvolvido para gerenciamento de jogadores de futebol, permitindo o cadastro e organização de jogadores por diferentes critérios como posição, pontuação, número de jogos e habilidades. O aplicativo é ideal para organizadores de peladas e pequenos campeonatos que precisam gerenciar seus jogadores.

## ✨ Funcionalidades

- **Cadastro de Jogadores**: Adicione jogadores com nome, posição principal e secundária, e avaliação por estrelas (overal)
- **Sistema de Pontuação**: Configure a pontuação por vitória, empate e derrota
- **Estatísticas**: Acompanhe vitórias, derrotas, empates e pontuação total dos jogadores
- **Filtragem Avançada**: Organize jogadores por diferentes critérios:
  - Nome (ordem alfabética)
  - Pontuação total
  - Número de jogos
  - Posição no campo
  - Avaliação por estrelas (overal)

## 🛠 Tecnologias Utilizadas

- **Linguagem**: Kotlin
- **UI**: Jetpack Compose
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Injeção de Dependência**: Hilt
- **Persistência**: Room Database
- **Gerenciamento de Estado**: StateFlow
- **Gerenciamento de Dependências**: Gradle KTS

## 📦 Estrutura do Projeto

O projeto segue uma arquitetura moderna e organizada:

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/victorhugo/boleiragem/
│   │   │   ├── data/
│   │   │   │   ├── model/ (Entidades e classes de dados)
│   │   │   │   ├── repository/ (Implementações de repositório)
│   │   │   ├── ui/
│   │   │   │   ├── screens/ (Telas Compose organizadas por funcionalidade)
│   │   │   │   │   ├── cadastro/ (Tela de cadastro de jogadores)
│   │   │   │   │   ├── configuracao/ (Configurações do aplicativo)
│   │   │   │   │   └── ...
```

## 🚀 Instalação e Uso

### Pré-requisitos

- Android Studio Iguana ou mais recente
- SDK Android 24 (Android 7.0) ou superior
- Kotlin 1.9.0 ou superior

### Como Executar

1. Clone o repositório:
   ```
   git clone https://github.com/ivaaxi/Boleiragem.git
   ```
   
2. Abra o projeto no Android Studio:
   ```
   File > Open > [selecione a pasta do projeto]
   ```
   
3. Execute a sincronização do Gradle

4. Execute o aplicativo em um emulador ou dispositivo físico:
   ```
   Run > Run 'app'
   ```

## 📷 Screenshots

[Aqui você pode adicionar screenshots do seu aplicativo]

## 🤝 Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir uma issue ou enviar um pull request.

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE)

## 📞 Contato

[Seu nome/informação de contato]

---

⭐️ Se você gostou deste projeto, por favor dê uma estrela no GitHub! ⭐️
