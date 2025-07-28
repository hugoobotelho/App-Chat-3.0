
# Aplicativo de Chat P2P em Rede Local

## 📋 Descrição

Aplicativo de chat peer-to-peer para comunicação em rede local, que permite criar grupos e trocar mensagens em tempo real, sem a necessidade de um servidor central.
Utiliza **sockets TCP e UDP** para transmissão e controle das mensagens.

---

## 🚀 Tecnologias Utilizadas

* **Java**
* **Sockets TCP e UDP**
* **Threads** para controle de concorrência

---

## 💡 Funcionalidades

* Comunicação direta entre dispositivos na mesma rede local
* Criação e gerenciamento de grupos de chat
* Envio e recebimento de mensagens em tempo real
* Comunicação sem servidor central, utilizando TCP e UDP

---

## 🛠️ Como Executar

1. Clone o repositório:

   ```bash
   git clone https://github.com/hugoobotelho/App-Chat-3.0.git
   ```

2. Acesse a pasta do projeto:

   ```bash
   cd App-Chat-3.0
   ```

3. Compile o arquivo principal:

   ```bash
   javac Principal.java
   ```

4. Execute o programa:

   ```bash
   java Principal
   ```

> **Importante:** Certifique-se de que os dispositivos estejam conectados à mesma rede local para que a comunicação funcione corretamente.

---

## ⚠️ Observações

* Projeto focado exclusivamente em redes locais; **não suporta comunicação pela internet**.
* Utiliza threads para permitir múltiplas conexões simultâneas.

---

## Licença
Este projeto está sob a licença MIT.
