namespace java com.taocoder.ourea

include 'fb303.thrift'

service Ourea extends fb303.FacebookService{

string queryEcho(1:string request)

}