# About
Project is TCP server of text communicator.
Server supports:
- registration request (with username and password) from client 
- login request from client
- request of another logged in user IP address

Once client gets IP address of second client it can directly connect to him, and 
start exchanging messages.
All massages (server-to-client, client-to-server, client-client) are defined in pwr_msg_proto directory.
<br>
<br>
<b>Server port: 8085</b>
<br><b>Fixed address IP recommended.</b>
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

# Communication with server
Communication objects (from client):
- register
```
PwrMsg.clinet_to_server.newBuilder().setTypeValue(0).setLoginString(login).setPasswordString(password).build();
```
- login
```
PwrMsg.clinet_to_server.newBuilder().setTypeValue(1).setLoginString(login).setPasswordString(password).build();
```
- getIP
```
PwrMsg.clinet_to_server.newBuilder().setTypeValue(2).setLoginString(login).build();
```
- logout
```
PwrMsg.clinet_to_server.newBuilder().setTypeValue(3).setLoginString(login).build();
```

## Requests
Before each query the connection should be initialized:
````
socket = new Socket("ipAddr", 8085);
outputStream = new DataOutputStream(socket.getOutputStream());
inputStream = new DataInputStream(socket.getInputStream());
````
After each query the connection should be closed:
````
socket.close();
````

## Sendind data
````
byte[] arr = toSend.toByteArray();
int length = 0;

outputStream.flush();
outputStream.writeInt(arr.length);
outputStream.write(arr);
````

## Receiving data
````
length = inputStream.readInt();
byte[] arr = new byte[length];

inputStream.read(arr, 0, arr.length);
toGet = PwrMsg.server_to_clinet.parseFrom(arr);
````

# JAR
Executable jar and database files (must be in the same folder!): 
``
out/artifacts/ChatServer
``