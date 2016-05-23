Sistema Distribuido N-Gram
O Sistema realiza a contagem da frequência com que uma letra vai pra outra dada uma base de dados de texto plano. O sistema trabalha de forma distribuida encontrando escravos na rede para enviar e processar os arquivos.


<h1>OBJETIVO</h1>

O objetivo do programa é criar um ambiente distribuído para processamento de informações sobre texto plano, ou seja, o sistema recebe uma base de texto plano e faz algum processamento sobre ele. O projeto foi feito sobre a base de texto do wikipédia BR, a primeira etapa foi exportar a base em texto plano, e a segunda etapa foi verificar a distribuição de frequência com que uma letra vai pra outra na língua portuguesa, o resultado final é um arquivo de texto indicando a frequência das letras como no exemplo abaixo, onde temos somente a palavra BANANA de texto e é retornado um arquivo com a frequência que B vai pra A e A vai pra N e por fim de N que vai para A:

 - BANANA <br/>
 B,A, 1 <br/>
 A,N, 2 <br/>
 N,A, 2


<h1>FUNCIONAMENTO</h1>

O sistema possui 5 classes, vou relata-las e dar uma breve explicação abaixo:

KeepAlive -> Envia o broadcast pela rede para encontrar novas máquinas que processem o texto.

Mestre -> Possui a base e os arquivos que deve enviar para os escravos, recebe os resultados e consolida num arquivo principal.

Escravo -> Recebe um broadcast do mestre, faz uma conexão com ele e aguarda os arquivos a serem processados, a cada novo arquivo ele envia o resultado para o mestre e então espera um novo arquivo até que todos os arquivos da base sejam processados.

GeraMatrizMaster -> Atualiza num conjunto unico os dados que os escravos enviam para o mestre, e exporta este conjunto final em um arquivo de texto.

GeraMatrizWorker -> Recebe o arquivo que o mestre enviou e processa ele, classe que efetua o processamento no escravo.

<h1>AUTORES</h1>

Este projeto foi desenvolvido na Faculdade de Tecnologia (FATEC) de Carapicuíba, pelos alunos Lucas Oliveira de Almeida e Luana Mantovani Cassiano da Silva, para a disciplina de Sistemas Distribuídos, ministrada pelo professor Evandro Luquini.
