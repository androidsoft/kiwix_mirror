/*-
 * Copyright (c) 2004 - 2010 CTPP Team
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 4. Neither the name of the CTPP Team nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *      FnURIEscape.cpp
 *
 * $CTPP$
 */

#include "CDT.hpp"
#include "CTPP2Logger.hpp"
#include "CTPP2Util.hpp"
#include "FnURIEscape.hpp"

namespace CTPP // C++ Template Engine
{
//
// Constructor
//
FnURIEscape::FnURIEscape()
{
	;;
}

//
// Handler
//
INT_32 FnURIEscape::Handler(CDT            * aArguments,
                            const UINT_32    iArgNum,
                            CDT            & oCDTRetVal,
                            Logger         & oLogger)
{
	if (iArgNum < 1)
	{
		oLogger.Emerg("Usage: URIESCAPE(data1, data2, ..., dataN); at least 1 argument need");
		return -1;
	}

	STLW::string sResult;
	for(INT_32 iPos = iArgNum - 1; iPos >=0; --iPos) { sResult.append(aArguments[iPos].GetString()); }

	oCDTRetVal = URIEscape(sResult);

return 0;
}

//
// Get function name
//
CCHAR_P FnURIEscape::GetName() const { return "uriescape"; }

//
// A destructor
//
FnURIEscape::~FnURIEscape() throw() { ;; }

} // namespace CTPP
// End.
