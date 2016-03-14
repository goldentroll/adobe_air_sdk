//
//  AdjustFunction.h
//  AdjustExtension
//
//  Created by Pedro Filipe on 07/08/14.
//  Copyright (c) 2014 adjust. All rights reserved.
//

#import "FlashRuntimeExtensions.h"
#import <AdjustSdk/Adjust.h>

@interface AdjustFunction: NSObject<AdjustDelegate>

- (void)adjustAttributionChanged:(ADJAttribution *)attribution;

@end

FREObject ADJonCreate(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJtrackEvent(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJsetEnabled(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJisEnabled(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJappWillOpenUrl(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJsetOfflineMode(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJsetDeviceToken(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJgetIdfa(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);

// Android methods
FREObject ADJonResume(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJonPause(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJsetReferrer(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject ADJgetGoogleAdId(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
