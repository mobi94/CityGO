//
//  AppDelegate.m
//  CityGo
//
//  Created by ruslan on 1/20/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "AppDelegate.h"
#import "CGParseAppDelegate.h"
#import "CGLoginViewController.h"
#import <FacebookSDK/FacebookSDK.h>

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    BOOL wasHandled = [FBAppCall handleOpenURL:url sourceApplication:sourceApplication fallbackHandler:^(FBAppCall *call)
                       {
                           if ([[call appLinkData] targetURL] != nil)
                           {
                               // get the object ID string from the deep link URL
                               // we use the substringFromIndex so that we can delete the leading '/' from the targetURL
                               NSString *objectId = [[[call appLinkData] targetURL].path substringFromIndex:1];
                               
                               // now handle the deep link
                               // write whatever code you need to show a view controller that displays the object, etc.
                               [[[UIAlertView alloc] initWithTitle:@"Directed from Facebook"
                                                           message:[NSString stringWithFormat:@"Deep link to %@", objectId]
                                                          delegate:self
                                                 cancelButtonTitle:@"OK!"
                                                 otherButtonTitles:nil] show];
                           }
                           else
                           {
                               //
                               NSLog(@"Unhandled deep link: %@", [[call appLinkData] targetURL]);
                           }
                       }];
    
    return wasHandled;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    [CGParseAppDelegate application:application parseDidFinishLaunchingWithOptions:launchOptions];
    
    if (![PFUser currentUser])
    {
        [self showLoginScreen:NO];
    }
    
    return YES;
}

- (void)showLoginScreen:(BOOL)animated
{
    // Get login screen from storyboard and present it
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    CGLoginViewController *viewController = (CGLoginViewController *)[storyboard instantiateViewControllerWithIdentifier:@"LoginViewController"];
    //[self.window makeKeyAndVisible];
    
    [self.window setRootViewController:viewController];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)newDeviceToken
{
    [PFPush storeDeviceToken:newDeviceToken];
    [PFPush subscribeToChannelInBackground:@"" target:self selector:@selector(subscribeFinished:error:)];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    if (error.code == 3010)
    {
        NSLog(@"Push notifications are not supported in the iOS Simulator.");
    }
    else
    {
        // show some alert or otherwise handle the failure to register.
        NSLog(@"application:didFailToRegisterForRemoteNotificationsWithError: %@", error);
    }
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    [PFPush handlePush:userInfo];
    
    if (application.applicationState == UIApplicationStateInactive)
    {
        [PFAnalytics trackAppOpenedWithRemoteNotificationPayload:userInfo];
    }
}


- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    [FBAppCall handleDidBecomeActive];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    [[FBSession activeSession] close];
}

#pragma mark - ()

- (void)subscribeFinished:(NSNumber *)result error:(NSError *)error
{
    if ([result boolValue])
    {
        NSLog(@"ParseStarterProject successfully subscribed to push notifications on the broadcast channel.");
    }
    else
    {
        NSLog(@"ParseStarterProject failed to subscribe to push notifications on the broadcast channel.");
    }
}

@end
