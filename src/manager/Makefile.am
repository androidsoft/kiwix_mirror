bin_PROGRAMS=kiwix-manage
AM_CPPFLAGS=-I$(top_srcdir)/src/common -I$(top_srcdir)/src/zimlib/include -I$(top_srcdir)/src/pugixml
kiwix_manage_SOURCES= \
	kiwix-manage.cpp \
	../common/kiwix/manager.cpp \
	../common/kiwix/library.cpp \
	../common/kiwix/reader.cpp \
	../common/base64.cpp \
	../common/regexTools.cpp \
	../common/stringTools.cpp \
	../common/pathTools.cpp \
	../common/kiwix/manager.h \
	../common/kiwix/library.h \
	../common/kiwix/reader.h \
	../common/base64.h \
	../common/regexTools.h \
	../common/stringTools.h \
	../common/pathTools.h
kiwix_manage_CXXFLAGS=$(ZIMLIB_CFLAGS) $(ICU_CFLAGS) $(LZMA_CFLAGS) -I../common/

kiwix_manage_LDFLAGS= $(CHECK_LDFLAGS)

if STATIC_BINS
kiwix_manage_LDFLAGS+= $(CHECK_LDFLAGS) $(ZIMLIB_STATIC_LDFLAGS) $(ICU_STATIC_LDFLAGS) ../zimlib/src/.libs/libzim.a ../pugixml/.libs/libpugixml.a $(LZMA_STATIC_LDFLAGS) $(STATIC_LIBM_ARCHIVE) $(STATIC_LIBSTDCPP_ARCHIVE) $(STATIC_LIBDL_ARCHIVE) $(STATIC_LIBGCC_ARCHIVE) $(STATIC_LIBPTHREAD_ARCHIVE) --static
else
kiwix_manage_LDFLAGS+= $(ZIMLIB_LDFLAGS) $(ICU_LDFLAGS) ../zimlib/src/.libs/libzim.a ../pugixml/.libs/libpugixml.a $(LZMA_LDFLAGS) -ldl
endif
