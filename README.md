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

<img width="261" height="476" alt="image" src="https://github.com/user-attachments/assets/826ea170-d889-4624-bc0a-d6fe866219f2" />
<img width="268" height="486" alt="image" src="https://github.com/user-attachments/assets/29dca5d0-a07d-4b4c-a003-eda043110080" />
<img width="265" height="484" alt="image" src="https://github.com/user-attachments/assets/5f0c9925-f476-4286-9c4a-58d4df234938" />
<img width="268" height="481" alt="image" src="https://github.com/user-attachments/assets/aa337399-9931-4462-8c4c-edd38f8b2dd8" />


## 🤝 Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir uma issue ou enviar um pull request.

## 📞 Contato

Nome: Victor Hugo
Tel.: (32)98470-2332

---

⭐️ Se você gostou deste projeto, por favor dê uma estrela no GitHub! ⭐️
