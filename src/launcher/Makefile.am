noinst_PROGRAMS=kiwix-launcher

AM_CPPFLAGS=-I$(top_srcdir)/src/common

kiwix_launcher_SOURCES= kiwix-launcher.cpp \
			../common/pathTools.cpp \
			../common/stringTools.cpp \
			../common/pathTools.h \
			../common/stringTools.h

kiwix_launcher_CXXFLAGS= $(ICU_CFLAGS) -I../common/
kiwix_launcher_LDFLAGS=$(CHECK_LDFLAGS)

if STATIC_BINS
kiwix_launcher_LDFLAGS+= $(STATIC_LIBM_ARCHIVE) $(ICU_STATIC_LDFLAGS) $(STATIC_LIBSTDCPP_ARCHIVE) $(STATIC_LIBDL_ARCHIVE) $(STATIC_LIBGCC_ARCHIVE) $(STATIC_LIBPTHREAD_ARCHIVE) --static
else
kiwix_launcher_LDFLAGS+= $(ICU_LDFLAGS) -ldl $(UUID_LDFLAGS) -lpthread
endif

