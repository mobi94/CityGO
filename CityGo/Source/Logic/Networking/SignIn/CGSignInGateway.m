//
//  CGSignInGateway.m
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGSignInGateway.h"
#import "CGFacebookGateway.h"

@implementation CGSignInGateway

- (void)signInUsingFBWithSuccess:(SignInSuccessBlock)successBlock failure:(SignInFailureBlock)failureBlock
{
    CGFacebookGateway *FBGateway = [CGFacebookGateway new];
    
    [FBGateway loginWithSuccess:^(NSDictionary *fbUser)
    {
        PFUser *user = [PFUser user];
        user.username = fbUser[@"username"];
        user.password = fbUser[@"password"];
        user.email = fbUser[@"email"];
        
        user[@"gender"] = fbUser[@"gender"];
        user[@"avatarFB"] = fbUser[@"avatarLink"];
//        user[@"age"] = fbUser[@"birthday"];
        
        [user signUpInBackgroundWithBlock:^(BOOL succeeded, NSError *error)
        {
            if (!error)
            {
                // Hooray! Let them use the app now.
                successBlock(succeeded);
            }
            else
            {
                if (error.code == 202)
                {
                    [PFUser logInWithUsernameInBackground:user.username password:user.password block:^(PFUser *user, NSError *error)
                    {
                        if(!error)
                        {
                            successBlock(YES);
                        }
                        else
                        {
                            failureBlock(error);
                        }
                    }];
                }
            }
        }];
        
    }
    failure:^(NSError *error)
    {
        failureBlock(error);
    }];
}

@end
