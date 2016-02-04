#include <stdafx.h>
#include <sapi.h>

int main(int argc, char* argv[])
{
	ISpVoice * pVoice = NULL;

	if (FAILED(::CoInitialize(NULL)))
		return FALSE;

	HRESULT hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&pVoice);
	if (SUCCEEDED(hr))
	{
		//hr = pVoice->Speak(L"Hello Brad <pitch middle = '-10'/> gwaps", SPF_IS_XML, NULL);
		//hr = pVoice->Speak(L"Myca needs vog", 0, NULL);
		//hr = pVoice->Speak(L"cliff golem tashed", 0, NULL);
		//hr = pVoice->Speak(L"Invis failing, invis failing", 0, NULL);

		hr = pVoice->Speak(L"charm break, <pitch middle = '-10'/> beat down imminent", SPF_IS_XML, NULL);
		pVoice->Release();
		pVoice = NULL;
	}

	::CoUninitialize();
	return TRUE;
}