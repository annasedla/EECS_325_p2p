# EECS_325

I had to make a few modifications to the file structure:

1. The file has to be run by calling java p2p/p2p rather than java p2p because I made a p2p package. This way I don't
 have any public static variables.

2. I placed config-peer inside config-neighbors such that the first line is the IP, query port and data port
of this peer and the rest of are the neighbors. I hope that is not an issue it was easier to configure in this way.

Regarding Note 2 in the protocols, I handle duplicate queries by storing all queries in a queries list and
before processing it determining if it is unique with respect to all the other queries in that list. If it is unique,
I then process it otherwise, it is discarded.

The files are in shared_10 to shared _15 however if you look on the servers it just says shared.