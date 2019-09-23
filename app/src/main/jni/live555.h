#ifndef  __LIVE555_HEAD_H__
#define __LIVE555_HEAD_H__

int live555_start(char *url);
int live555_stop();
int live555_requestFrameBuffer(unsigned char **pdata,int *plen);
int live555_releaseBuffer(int index);

int live555_getFrame(unsigned char *data, int len);
#endif