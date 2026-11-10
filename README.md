# NexRain - API Restful (Java / Spring Boot)

API do NexRain, solução da Global Solution 2026 (2º semestre) - CitizenTech. Monitora alagamentos urbanos
em São Paulo juntando três fontes:

- **Visão computacional**: analisa imagens de câmeras urbanas (ou fotos enviadas pelo cidadão) com a IA da
  OpenAI (GPT-4o-mini) e registra a ocorrência com local, horário, altura da água e nível de risco.
  Sem a chave da OpenAI, usa um motor local de análise de cor e textura da imagem.
- **Previsão de chuva real**: consulta a API pública [Open-Meteo](https://open-meteo.com) e calcula o
  risco de alagamento de cada região cruzando chuva prevista, histórico do bairro e ocorrências ativas.
- **Rotas seguras**: calcula o trajeto de carro pelo [OSRM](https://project-osrm.org) (dados do
  OpenStreetMap) e um algoritmo próprio desvia dos pontos alagados.

Tem também o CRUD completo de regiões, câmeras, ocorrências, alertas e rotas no banco Oracle da FIAP.

Equipe NexRain:

- Mateus Saavedra De Mendonça Cruz - RM563266
- Davi Melo - RM561818
- Danilo Fernandes - RM561657

A documentação completa (endpoints, protótipos, DER e diagrama de classes) está em
`documentacao/NexRain_Documentacao_GS.pdf`.

## Projeto publicado

- Site (Vercel): https://nexrain-front.vercel.app/
- API (Render): https://nexrain-springboot.onrender.com

A API usa o plano gratuito do Render, que desliga depois de um tempo sem uso. O primeiro acesso pode
levar cerca de um minuto.

## O que precisa ter instalado

- Java 21
- Não precisa instalar o Maven, o projeto já vem com o `mvnw`

## Como rodar

- 1 Banco de dados

Rodar o arquivo `script.sql` inteiro **como script** conectado ao Oracle da FIAP: no DBeaver `Alt+X`, no SQL Developer `F5`
(não use `Ctrl+Enter`, que executa um comando só)
(`oracle.fiap.com.br:1521:orcl`). Ele cria as 5 tabelas e já cadastra 12 regiões de São Paulo, 8 câmeras,
4 ocorrências e 2 alertas de exemplo. São 18 câmeras: 10 com imagem ao vivo das câmeras públicas da CET-SP
(cameras.cetsp.com.br) e 8 com fotos de enchentes reais para demonstrar a detecção.

Se o banco já foi criado com uma versão anterior do script, rode só o `cameras_ao_vivo.sql` para incluir as câmeras ao vivo.

Para recriar o banco do zero, rode antes o `drop.sql` (que apaga as tabelas) e depois o `script.sql` de novo.

- 2 Usuário, senha e chave da OpenAI

Essas informações não ficam no código. Para rodar na sua máquina, crie o arquivo
`src/main/resources/local.properties` com:

```properties
db.usuario=seu usuario do Oracle (ex: RM563266)
db.senha=sua senha do Oracle
openai.api.key=sua chave da OpenAI
```

Esse arquivo não vai para o GitHub. A chave da OpenAI é opcional: se ficar vazia, a análise das imagens
é feita pelo motor local. As APIs de clima e mapas são públicas e não precisam de chave.

- 3 Subir a API

```bash
.\mvnw spring-boot:run
```

Ou abrir o projeto no IntelliJ e rodar a classe `NexrainApplication`.

A API sobe em `http://localhost:8080`. Quando aparecer `Started NexrainApplication` no console ela
já está no ar. Para conferir, abra `http://localhost:8080/api/health` no navegador. O campo `ia` mostra
se está usando `openai` ou `motor-local`.

Para testar só a regra de risco com a previsão real (sem banco), rode a classe
`br.com.fiap.nexrain.main.TesteMonitorRisco`.

## Deploy no Render

New > Web Service, conectar este repositório e escolher `Docker` como linguagem (usa o `Dockerfile`
do projeto). Em Health Check Path, colocar `/api/health`.

Variáveis de ambiente (Environment):

| Variável | Valor |
|---|---|
| `DB_USUARIO` | usuário do Oracle da FIAP |
| `DB_SENHA` | senha do Oracle da FIAP |
| `OPENAI_API_KEY` | chave da OpenAI (opcional) |

## Principais endpoints

| Verbo | URI | O que faz |
|---|---|---|
| POST | `/api/monitoramento/analises` | Analisa a imagem de uma câmera ou foto e registra a ocorrência |
| GET | `/api/riscos?simulacao=false` | Risco de alagamento por região (previsão real ou temporal simulado) |
| GET | `/api/clima?latitude=&longitude=` | Clima atual e chuva prevista das próximas 24h |
| POST | `/api/rotas` | Calcula a rota segura desviando dos alagamentos |
| PUT | `/api/ocorrencias/{id}/validacao` | Confirma, descarta ou resolve uma ocorrência |
| POST | `/api/alertas/previsao` | Gera alertas para as regiões com risco alto ou crítico |

A lista completa (CRUD de regiões, câmeras, ocorrências, alertas e rotas) e os códigos de status estão
na documentação em PDF.

## Créditos

- Previsão do tempo: Open-Meteo (CC BY 4.0)
- Mapas e rotas: OpenStreetMap / OSRM / Nominatim (ODbL)
- Imagens das câmeras de demonstração: Wikimedia Commons (licenças Creative Commons, autores
  citados no site)
