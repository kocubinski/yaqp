// tts.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include <sapi.h>
#include "tts.h"

ISpVoice* __cdecl create_voice()
{
	ISpVoice * pVoice = NULL;

	if (FAILED(::CoInitialize(NULL)))
		return FALSE;

	HRESULT hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&pVoice);
	return pVoice;
}

int __cdecl destroy_voice(ISpVoice* pVoice)
{
	pVoice->Release();
	pVoice = NULL;
	::CoUninitialize();
	return 0;
}

HRESULT __cdecl say_words(ISpVoice* pVoice, LPCWSTR words)
{
	//hr = pVoice->Speak(L"Hello Brad <pitch middle = '-10'/> gwaps", SPF_IS_XML, NULL);
	//hr = pVoice->Speak(L"Myca needs vog", 0, NULL);
	//hr = pVoice->Speak(L"cliff golem tashed", 0, NULL);
	//hr = pVoice->Speak(L"Invis failing, invis failing", 0, NULL);
	//hr = pVoice->Speak(L"charm break, <pitch middle = '-10'/> beat down imminent", SPF_IS_XML, NULL);

	HRESULT hr = pVoice->Speak(words, 0, NULL);
	return hr;
}
