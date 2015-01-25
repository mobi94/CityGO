//
//  CGParseAppDelegate.m
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGParseAppDelegate.h"

@implementation CGParseAppDelegate

+ (void)application:(UIApplication *)application parseDidFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Enable storing and querying data from Local Datastore. Remove this line if you don't want to
    // use Local Datastore features or want to use cachePolicy.
    [Parse enableLocalDatastore];
    
    // ****************************************************************************
    // Uncomment this line if you want to enable Crash Reporting
    // [ParseCrashReporting enable];

    [Parse setApplicationId:@"OZYscvQi1cKHCP0vo6hPbbGAPvWs6M6vuMvrMRHi" clientKey:@"CTt2KMlNavNfboR5Tt0f8bA0I0h38ZgCFPtE6I5s"];
    
    if (application.applicationState != UIApplicationStateBackground)
    {
        // Track an app open here if we launch with a push, unless
        // "content_available" was used to trigger a background push (introduced in iOS 7).
        // In that case, we skip tracking here to avoid double counting the app-open.
        BOOL preBackgroundPush = ![application respondsToSelector:@selector(backgroundRefreshStatus)];
        BOOL oldPushHandlerOnly = ![self respondsToSelector:@selector(application:didReceiveRemoteNotification:fetchCompletionHandler:)];
        BOOL noPushPayload = ![launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
        if (preBackgroundPush || oldPushHandlerOnly || noPushPayload)
        {
            [PFAnalytics trackAppOpenedWithLaunchOptions:launchOptions];
        }
    }
    
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 80000
    if ([application respondsToSelector:@selector(registerUserNotificationSettings:)]) {
        UIUserNotificationType userNotificationTypes = (UIUserNotificationTypeAlert |
                                                        UIUserNotificationTypeBadge |
                                                        UIUserNotificationTypeSound);
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:userNotificationTypes
                                                                                 categories:nil];
        [application registerUserNotificationSettings:settings];
        [application registerForRemoteNotifications];
    } else
#endif
    {
        [application registerForRemoteNotificationTypes:(UIRemoteNotificationTypeBadge |
                                                         UIRemoteNotificationTypeAlert |
                                                         UIRemoteNotificationTypeSound)];
    }
}

@end
