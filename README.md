# About
Project is TCP server of text communicator.
Server supports:
- registration request (with username and password) from client 
- login request from client
- request of another logged in user IP address

Once client gets IP address of second client it can directly connect to him, and 
start exchanging messages.
All massages (server-to-client, client-to-server, client-client) are defined in pwr_msg_proto directory.

## pwr_msg_proto directory 
pwr_msg_proto is git submodule (https://github.com/MichalGrzesiak/pwr_msg_proto)
that contains proto definitions of sent messages.
To pull that repository run
```
git submodule --init --recursive
```

# Prerequisites
- maven 
- Java


# Compilation
To compile run 
```
mvn clean install
```


# Run
To run 
```
java -cp target/Chat-1.0-SNAPSHOT.jar pl.Main
```

