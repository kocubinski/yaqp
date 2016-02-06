#include "stdafx.h"
#include <sapi.h>

#define DLLEXPORT  __declspec(dllexport)
DLLEXPORT ISpVoice* __cdecl create_voice();
DLLEXPORT int __cdecl destory_voice(ISpVoice* pVoice);
DLLEXPORT HRESULT __cdecl say_words(LPCWSTR text);