For Client:

usage: Client [Port #] <server-name> [-w] <file-name> [-s StartBlock] [-e LastBlock]

arguments surrounded by [] can be written in any order. However, <server-name> must come before <file-name>.

For Server:

usage: Server [DEBUG=1] [Port <port_number>]

Arguments can be written in either order.


Note: When the value for -e exceeds the number of bytes within the file, the program will assume the maximum number of bytes from the file.
Another Note: when -w is invoked, content from -s and -e are ignored.

Files called by the server or client needs to be in the same directory of the programs.